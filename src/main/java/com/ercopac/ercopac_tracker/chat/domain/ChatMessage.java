package com.ercopac.ercopac_tracker.chat.domain;

import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession session;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private AppUser sender;

    private String content;
    private Instant sentAt;
    private boolean isInternal;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ChatSession getSession() { return session; }
    public void setSession(ChatSession session) { this.session = session; }
    public AppUser getSender() { return sender; }
    public void setSender(AppUser sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public boolean isInternal() { return isInternal; }
    public void setInternal(boolean internal) { isInternal = internal; }
}