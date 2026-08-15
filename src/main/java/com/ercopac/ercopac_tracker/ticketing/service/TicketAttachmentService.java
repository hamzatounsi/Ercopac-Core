package com.ercopac.ercopac_tracker.ticketing.service;
import com.ercopac.ercopac_tracker.ticketing.domain.*; 
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.TicketAttachmentDto; 
import com.ercopac.ercopac_tracker.ticketing.repository.TicketAttachmentRepository; 
import jakarta.transaction.Transactional; 
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.core.io.*; 
import org.springframework.http.HttpStatus; 
import org.springframework.stereotype.Service; 
import org.springframework.web.multipart.MultipartFile; 
import org.springframework.web.server.ResponseStatusException; 
import java.io.IOException; 
import java.nio.file.*; 
import java.util.*;
@Service @Transactional public class TicketAttachmentService {
 private static final Set<String> TYPES=Set.of("application/pdf","image/png","image/jpeg","text/plain","application/vnd.openxmlformats-officedocument.wordprocessingml.document"); 
 private final TicketAttachmentRepository repository; 
 private final TicketService tickets; 
 private final Path root;
 public TicketAttachmentService(TicketAttachmentRepository repository,TicketService tickets,@Value("${ticketing.storage.path:uploads/tickets}") String root){
    this.repository=repository;
    this.tickets=tickets;
    this.root=Paths.get(root).toAbsolutePath().normalize();
}
 public TicketAttachmentDto upload(Long ticketId,MultipartFile file){
    Ticket ticket=tickets.requireAccessible(ticketId);
    if(file==null||file.isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A non-empty attachment is required.");
    if(file.getSize()>10*1024*1024)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Attachment must not exceed 10 MB.");
    String type=Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");
    if(!TYPES.contains(type))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported attachment type.");String original=sanitizeOriginalFileName(file.getOriginalFilename());
    String stored=UUID.randomUUID()+extensionForContentType(type);
    Path target=root.resolve(stored).normalize();
    if(!target.startsWith(root))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid attachment storage path.");
    try{Files.createDirectories(root);Files.copy(file.getInputStream(),target,StandardCopyOption.REPLACE_EXISTING);}
    catch(IOException e){throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not store attachment.");}TicketAttachment a=new TicketAttachment();a.setTicket(ticket);a.setOriginalFileName(original);a.setStoredFileName(stored);a.setContentType(type);a.setFileSize(file.getSize());a.setStoragePath(stored);a.setUploadedBy(tickets.currentUser());a=repository.save(a);
    return dto(a);} public Resource download(Long ticketId,Long attachmentId){tickets.requireAccessible(ticketId);TicketAttachment a=repository.findByIdAndTicket_Id(attachmentId,ticketId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Attachment not found."));Path p=root.resolve(a.getStoragePath()).normalize();if(!p.startsWith(root)||!Files.isRegularFile(p))
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Attachment file not found.");
    return new FileSystemResource(p);} public void delete(Long ticketId,Long id){TicketAttachment a=repository.findByIdAndTicket_Id(id,ticketId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Attachment not found."));
    if(!tickets.mayManageCurrent()&&!a.getUploadedBy().getId().equals(tickets.currentUser().getId()))
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the uploader or a manager may remove this attachment.");
    Path target=root.resolve(a.getStoragePath()).normalize();
    if(!target.startsWith(root))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid attachment storage path.");
    try{Files.deleteIfExists(target);}
    catch(IOException ignored){}repository.delete(a);} private String extensionForContentType(String contentType){return switch(contentType){
        case "application/pdf" -> ".pdf";
        case "image/png" -> ".png";
        case "image/jpeg" -> ".jpg";
        case "text/plain" -> ".txt";
        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
        default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported attachment type.");
    };}private String sanitizeOriginalFileName(String fileName){String name=Optional.ofNullable(fileName).orElse("attachment").replace('\\','/');
        int lastSlash=name.lastIndexOf('/');name=lastSlash>=0?name.substring(lastSlash+1):name;
        return name.replaceAll("[^a-zA-Z0-9._ -]","_");}private TicketAttachmentDto dto(TicketAttachment a){var u=a.getUploadedBy();
        return new TicketAttachmentDto(a.getId(),a.getOriginalFileName(),a.getContentType(),a.getFileSize(),
        new com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.UserSummary(u.getId(),u.getFullName(),u.getEmail(),u.getRole().name()),a.getUploadedAt());
    }
}
