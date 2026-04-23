package com.ercopac.ercopac_tracker.projectum.actions.dto;

import java.time.LocalDateTime;

public class ActionCommentDto {
    private Long id;
    private String author;
    private String text;
    private LocalDateTime createdAt;

    public ActionCommentDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}