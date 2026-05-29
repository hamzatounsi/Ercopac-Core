package com.ercopac.ercopac_tracker.projectum.change_requests.dto;

import org.springframework.core.io.Resource;

public class ChangeRequestAttachmentDownload {
    private final String fileName;
    private final Resource resource;

    public ChangeRequestAttachmentDownload(String fileName, Resource resource) {
        this.fileName = fileName;
        this.resource = resource;
    }

    public String getFileName() { return fileName; }
    public Resource getResource() { return resource; }
}