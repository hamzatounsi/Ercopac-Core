package com.ercopac.ercopac_tracker.crm.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "crm_opportunity_attachments")
public class CrmOpportunityAttachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "opportunity_id", nullable = false) private CrmOpportunity opportunity;
    @Column(name = "original_file_name", nullable = false, length = 255) private String originalFileName;
    @Column(name = "stored_file_name", nullable = false, unique = true, length = 255) private String storedFileName;
    @Column(name = "content_type", nullable = false, length = 120) private String contentType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    @Column(name = "storage_path", nullable = false, length = 500) private String storagePath;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "uploaded_by_id", nullable = false) private AppUser uploadedBy;
    @Column(name = "uploaded_at", nullable = false, updatable = false) private LocalDateTime uploadedAt = LocalDateTime.now();
    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation value) { organisation = value; }
    public CrmOpportunity getOpportunity() { return opportunity; }
    public void setOpportunity(CrmOpportunity value) { opportunity = value; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String value) { originalFileName = value; }
    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String value) { storedFileName = value; }
    public String getContentType() { return contentType; }
    public void setContentType(String value) { contentType = value; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long value) { fileSize = value; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String value) { storagePath = value; }
    public AppUser getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(AppUser value) { uploadedBy = value; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
