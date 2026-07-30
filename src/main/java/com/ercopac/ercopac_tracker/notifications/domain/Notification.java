package com.ercopac.ercopac_tracker.notifications.domain;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long organisationId;
    private Long projectId;
    private Long taskId;
    private Long recipientUserId;
    private String recipientEmail;
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    private String severity;
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String message;
    @Column(columnDefinition = "TEXT")
    private String htmlBody;
    private int retryCount;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime nextRetryAt;

    @Column(nullable = false)
    private boolean readByUser = false;

    private String link;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrganisationId() {
        return organisationId;
    }
    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }
    public Long getProjectId() {
        return projectId;
    }
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    public Long getTaskId() {
        return taskId;
    }
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    public Long getRecipientUserId() {
        return recipientUserId;
    }
    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }
    public String getRecipientEmail() {
        return recipientEmail;
    }
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    public NotificationChannel getChannel() {
        return channel;
    }
    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }
    public NotificationStatus getStatus() {
        return status;
    }
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
    public String getSeverity() {
        return severity;
    }
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getHtmlBody() {
        return htmlBody;
    }
    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }
    public int getRetryCount() {
        return retryCount;
    }
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }
    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }
    public boolean isReadByUser() {
        return readByUser;
    }
    public void setReadByUser(boolean readByUser) {
        this.readByUser = readByUser;
    }
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
}