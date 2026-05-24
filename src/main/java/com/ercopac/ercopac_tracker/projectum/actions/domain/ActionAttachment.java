package com.ercopac.ercopac_tracker.projectum.actions.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "action_attachments")
public class ActionAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_item_id", nullable = false)
    private ActionItem actionItem;

    @Column(nullable = false)
    private String fileName;

    private String contentType;

    private Long fileSize;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActionItem getActionItem() {
        return actionItem;
    }

    public void setActionItem(ActionItem actionItem) {
        this.actionItem = actionItem;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    // getters/setters

    
}