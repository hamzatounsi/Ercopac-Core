package com.ercopac.ercopac_tracker.projectum.actions.dto;

public class ActionSummaryDto {
    private long total;
    private long open;
    private long todo;
    private long doing;
    private long review;
    private long blocked;
    private long done;
    private long overdue;
    private long dueThisWeek;
    private long highPriorityOpen;
    private long customerVisible;
    private long internalOnly;
    private double completionRate;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getOpen() { return open; }
    public void setOpen(long open) { this.open = open; }

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

    public long getOverdue() { return overdue; }
    public void setOverdue(long overdue) { this.overdue = overdue; }

    public long getDueThisWeek() { return dueThisWeek; }
    public void setDueThisWeek(long dueThisWeek) { this.dueThisWeek = dueThisWeek; }

    public long getHighPriorityOpen() { return highPriorityOpen; }
    public void setHighPriorityOpen(long highPriorityOpen) { this.highPriorityOpen = highPriorityOpen; }

    public long getCustomerVisible() { return customerVisible; }
    public void setCustomerVisible(long customerVisible) { this.customerVisible = customerVisible; }

    public long getInternalOnly() { return internalOnly; }
    public void setInternalOnly(long internalOnly) { this.internalOnly = internalOnly; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
}