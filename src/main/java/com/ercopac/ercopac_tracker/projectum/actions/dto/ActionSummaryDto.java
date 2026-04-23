package com.ercopac.ercopac_tracker.projectum.actions.dto;

public class ActionSummaryDto {
    private long total;
    private long todo;
    private long doing;
    private long review;
    private long blocked;
    private long done;
    private long customerVisible;
    private long internalOnly;
    private long overdue;

    public ActionSummaryDto() {}

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getTodo() { return todo; }
    public void setTodo(long todo) { this.todo = todo; }

    public long getDoing() { return doing; }
    public void setDoing(long doing) { this.doing = doing; }

    public long getReview() { return review; }
    public void setReview(long review) { this.review = review; }

    public long getBlocked() { return blocked; }
    public void setBlocked(long blocked) { this.blocked = blocked; }

    public long getDone() { return done; }
    public void setDone(long done) { this.done = done; }

    public long getCustomerVisible() { return customerVisible; }
    public void setCustomerVisible(long customerVisible) { this.customerVisible = customerVisible; }

    public long getInternalOnly() { return internalOnly; }
    public void setInternalOnly(long internalOnly) { this.internalOnly = internalOnly; }

    public long getOverdue() { return overdue; }
    public void setOverdue(long overdue) { this.overdue = overdue; }
}