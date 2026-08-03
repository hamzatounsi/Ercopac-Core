package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketStatusTransitionValidatorTest {
    private final TicketStatusTransitionValidator validator = new TicketStatusTransitionValidator();

    @Test
    void permitsConfiguredLifecycleTransitions() {
        assertDoesNotThrow(() -> validator.validate(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, false));
        assertDoesNotThrow(() -> validator.validate(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, false));
        assertDoesNotThrow(() -> validator.validate(TicketStatus.RESOLVED, TicketStatus.CLOSED, false));
    }

    @Test
    void rejectsInvalidLifecycleTransitions() {
        assertThrows(TicketConflictException.class,
                () -> validator.validate(TicketStatus.OPEN, TicketStatus.CLOSED, false));
        assertThrows(TicketConflictException.class,
                () -> validator.validate(TicketStatus.CLOSED, TicketStatus.REOPENED, false));
    }
}
