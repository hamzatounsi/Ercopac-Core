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
 private static final Set<String> TYPES=Set.of("application/pdf","image/png","image/jpeg","image/gif","image/webp","text/plain","application/vnd.openxmlformats-officedocument.wordprocessingml.document","video/mp4","video/webm","video/quicktime");
 public static final long MAX_FILE_BYTES=10L*1024*1024; public static final long MAX_VIDEO_BYTES=20L*1024*1024;
 // Browsers send no / generic types for some files (e.g. .mov on Windows): fall back on the extension
 private static final Map<String,String> BY_EXTENSION=Map.of(".mp4","video/mp4",".webm","video/webm",".mov","video/quicktime",".png","image/png",".jpg","image/jpeg",".jpeg","image/jpeg",".gif","image/gif",".webp","image/webp",".pdf","application/pdf");

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
    String type=contentType(file);
    if(type.startsWith("video/")&&file.getSize()>MAX_VIDEO_BYTES)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Videos must not exceed 20 MB.");
    if(!type.startsWith("video/")&&file.getSize()>MAX_FILE_BYTES)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Attachment must not exceed 10 MB.");
    if(!TYPES.contains(type))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported attachment type.");String original=sanitizeOriginalFileName(file.getOriginalFilename());
    String stored=UUID.randomUUID()+extensionForContentType(type);
    Path target=root.resolve(stored).normalize();
    if(!target.startsWith(root))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid attachment storage path.");
    try{Files.createDirectories(root);Files.copy(file.getInputStream(),target,StandardCopyOption.REPLACE_EXISTING);}
    catch(IOException e){throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not store attachment.");}TicketAttachment a=new TicketAttachment();a.setTicket(ticket);a.setOriginalFileName(original);a.setStoredFileName(stored);a.setContentType(type);a.setFileSize(file.getSize());a.setStoragePath(stored);a.setUploadedBy(tickets.currentUser());a=repository.save(a);
    return dto(a);} /** Metadata of an attachment of an accessible ticket (content type and name for the download response). */
 public TicketAttachmentDto metadata(Long ticketId,Long attachmentId){tickets.requireAccessible(ticketId);return dto(repository.findByIdAndTicket_Id(attachmentId,ticketId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Attachment not found.")));}
 /** Deletes a ticket (TicketService.delete checks the permission) and then its stored attachment files. */
 public void deleteTicket(Long ticketId){for(String stored:tickets.delete(ticketId)){Path target=root.resolve(stored).normalize();if(target.startsWith(root)){try{Files.deleteIfExists(target);}catch(IOException ignored){}}}}
 private static String contentType(MultipartFile file){String type=Optional.ofNullable(file.getContentType()).orElse("application/octet-stream").toLowerCase(Locale.ROOT);if(TYPES.contains(type))return type;String name=Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);int dot=name.lastIndexOf('.');return dot<0?type:BY_EXTENSION.getOrDefault(name.substring(dot),type);}
 public Resource download(Long ticketId,Long attachmentId){tickets.requireAccessible(ticketId);TicketAttachment a=repository.findByIdAndTicket_Id(attachmentId,ticketId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Attachment not found."));Path p=root.resolve(a.getStoragePath()).normalize();if(!p.startsWith(root)||!Files.isRegularFile(p))
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
        case "image/gif" -> ".gif";
        case "image/webp" -> ".webp";
        case "video/mp4" -> ".mp4";
        case "video/webm" -> ".webm";
        case "video/quicktime" -> ".mov";
        case "text/plain" -> ".txt";
        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
        default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported attachment type.");
    };}private String sanitizeOriginalFileName(String fileName){String name=Optional.ofNullable(fileName).orElse("attachment").replace('\\','/');
        int lastSlash=name.lastIndexOf('/');name=lastSlash>=0?name.substring(lastSlash+1):name;
        return name.replaceAll("[^a-zA-Z0-9._ -]","_");}private TicketAttachmentDto dto(TicketAttachment a){var u=a.getUploadedBy();
        return new TicketAttachmentDto(a.getId(),a.getOriginalFileName(),a.getContentType(),a.getFileSize(),
        new com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.UserSummary(u.getId(),u.getFullName(),u.getEmail(),u.getPrimaryRole().name()),a.getUploadedAt());
    }
}
