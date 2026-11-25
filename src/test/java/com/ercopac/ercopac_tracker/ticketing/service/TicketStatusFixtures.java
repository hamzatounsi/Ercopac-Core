package com.ercopac.ercopac_tracker.ticketing.service;

import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import java.util.List;

/** Standard ticket statuses as unsaved-but-identified entities for unit tests. */
final class TicketStatusFixtures {
    private TicketStatusFixtures() {}

    static TicketStatus status(long id, String code, boolean closed, boolean isDefault) {
        TicketStatus s = new TicketStatus();
        s.setId(id); s.setCode(code); s.setName(code.charAt(0) + code.substring(1).toLowerCase().replace('_', ' '));
        s.setColor("blue"); s.setClosed(closed); s.setDefault(isDefault); s.setActive(true); s.setDisplayOrder((int) id);
        return s;
    }

    /** Standard status by code (ids 1..7 in the standard order). */
    static TicketStatus standard(String code) {
        List<TicketStatusService.Seed> all = TicketStatusService.STANDARD;
        for (int i = 0; i < all.size(); i++)
            if (all.get(i).code().equals(code)) return status(i + 1, code, all.get(i).closed(), all.get(i).isDefault());
        throw new IllegalArgumentException(code);
    }
}
