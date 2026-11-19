package com.ercopac.ercopac_tracker.user;

/**
 * Kind of account. INTERNAL: staff of the organisation (every account created before customer
 * users existed). CUSTOMER: a person at one of the organisation's customers; it signs in to the
 * Ticketing app only and always has exactly the CLIENT role and one customer (users.customer_id).
 * Not to be confused with AppUser.internalUser, which marks planning resources.
 */
public enum UserType {
    INTERNAL,
    CUSTOMER
}
