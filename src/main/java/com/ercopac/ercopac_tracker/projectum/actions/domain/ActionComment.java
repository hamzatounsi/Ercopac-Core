package com.ercopac.ercopac_tracker.projectum.actions.domain;

import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_comments")
public class ActionComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_item_id", nullable = false)
    private ActionItem actionItem;

    @Column(nullable = false, length = 120)
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id")
    private AppUser authorUser;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ActionComment() {}

    public Long getId() { return id; }

    public ActionItem getActionItem() { return actionItem; }
    public void setActionItem(ActionItem actionItem) { this.actionItem = actionItem; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public AppUser getAuthorUser() { return authorUser; }
    public void setAuthorUser(AppUser authorUser) { this.authorUser = authorUser; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
