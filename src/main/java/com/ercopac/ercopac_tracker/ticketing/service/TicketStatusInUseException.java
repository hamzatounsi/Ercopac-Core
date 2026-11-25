package com.ercopac.ercopac_tracker.ticketing.service;

/** A status still used by tickets cannot be deleted: reassign its tickets or deactivate it instead (HTTP 409). */
public class TicketStatusInUseException extends RuntimeException {
    private final long ticketCount;
    public TicketStatusInUseException(String message, long ticketCount) { super(message); this.ticketCount = ticketCount; }
    public long getTicketCount() { return ticketCount; }
}
