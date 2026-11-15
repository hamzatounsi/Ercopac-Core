package com.ercopac.ercopac_tracker.customer.dto;

/**
 * Where a customer is used. A customer with any reference cannot be deleted, only deactivated.
 *
 * @param projects         projects linked by customer_id, or by their legacy customer text
 * @param tickets          tickets linked to the customer
 * @param ticketingRecords ticketing sites, contacts, equipment and order records of the customer
 */
public record CustomerReferences(long projects, long tickets, long ticketingRecords) {

    public long total() {
        return projects + tickets + ticketingRecords;
    }

    public boolean inUse() {
        return total() > 0;
    }
}
