package com.ercopac.ercopac_tracker.ticketing.domain;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="ticket_attachments") public class TicketAttachment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ticket_id",nullable=false) private Ticket ticket;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="message_id") private TicketMessage message;
 @Column(name="original_file_name",nullable=false,length=255) private String originalFileName; @Column(name="stored_file_name",nullable=false,unique=true,length=255) private String storedFileName;
 @Column(name="content_type",nullable=false,length=120) private String contentType; @Column(name="file_size",nullable=false) private long fileSize; @Column(name="storage_path",nullable=false,length=500) private String storagePath;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="uploaded_by_id",nullable=false) private AppUser uploadedBy; @Column(name="uploaded_at",nullable=false,updatable=false) private Instant uploadedAt;
 @PrePersist void created(){uploadedAt=Instant.now();} public Long getId(){return id;} public Ticket getTicket(){return ticket;} public void setTicket(Ticket v){ticket=v;} public TicketMessage getMessage(){return message;} public void setMessage(TicketMessage v){message=v;} public String getOriginalFileName(){return originalFileName;} public void setOriginalFileName(String v){originalFileName=v;} public String getStoredFileName(){return storedFileName;} public void setStoredFileName(String v){storedFileName=v;} public String getContentType(){return contentType;} public void setContentType(String v){contentType=v;} public long getFileSize(){return fileSize;} public void setFileSize(long v){fileSize=v;} public String getStoragePath(){return storagePath;} public void setStoragePath(String v){storagePath=v;} public AppUser getUploadedBy(){return uploadedBy;} public void setUploadedBy(AppUser v){uploadedBy=v;} public Instant getUploadedAt(){return uploadedAt;}
}
