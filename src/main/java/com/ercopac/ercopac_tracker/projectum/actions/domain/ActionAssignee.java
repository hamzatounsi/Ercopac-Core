package com.ercopac.ercopac_tracker.projectum.actions.domain;

import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "action_assignees")
public class ActionAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_item_id", nullable = false)
    private ActionItem actionItem;

    @Column(nullable = false, length = 120)
    private String assigneeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private AppUser assigneeUser;

    public ActionAssignee() {}

    public Long getId() { return id; }

    public ActionItem getActionItem() { return actionItem; }
    public void setActionItem(ActionItem actionItem) { this.actionItem = actionItem; }

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public AppUser getAssigneeUser() { return assigneeUser; }
    public void setAssigneeUser(AppUser assigneeUser) { this.assigneeUser = assigneeUser; }
}
