package com.ercopac.ercopac_tracker.customer.service;

import com.ercopac.ercopac_tracker.customer.dto.CustomerReferences;

/**
 * 409 with a stable error code the frontend translates:
 * CUSTOMER_NAME_EXISTS, CUSTOMER_CODE_EXISTS or CUSTOMER_IN_USE (with the reference counts).
 */
public class CustomerConflictException extends RuntimeException {

    public static final String NAME_EXISTS = "CUSTOMER_NAME_EXISTS";
    public static final String CODE_EXISTS = "CUSTOMER_CODE_EXISTS";
    public static final String IN_USE = "CUSTOMER_IN_USE";

    private final String code;
    private final CustomerReferences references;

    public CustomerConflictException(String code, String message, CustomerReferences references) {
        super(message);
        this.code = code;
        this.references = references;
    }

    public static CustomerConflictException nameExists(String name) {
        return new CustomerConflictException(NAME_EXISTS, "A customer named " + name + " already exists in this organisation.", null);
    }

    public static CustomerConflictException codeExists(String code) {
        return new CustomerConflictException(CODE_EXISTS, "Customer code " + code + " is already used.", null);
    }

    public static CustomerConflictException inUse(CustomerReferences references) {
        return new CustomerConflictException(IN_USE,
                "This customer is used by " + references.projects() + " project(s), " + references.tickets()
                        + " ticket(s) and " + references.ticketingRecords()
                        + " ticketing record(s). Deactivate it instead of deleting it.",
                references);
    }

    public String getCode() {
        return code;
    }

    public CustomerReferences getReferences() {
        return references;
    }
}
