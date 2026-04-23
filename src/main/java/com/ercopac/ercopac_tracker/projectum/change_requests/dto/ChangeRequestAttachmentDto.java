package com.ercopac.ercopac_tracker.projectum.change_requests.dto;

public class ChangeRequestAttachmentDto {
    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;

    public ChangeRequestAttachmentDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}