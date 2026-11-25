package com.ercopac.ercopac_tracker.ticketing.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;

/**
 * Ticket status of an organisation, managed in Ticketing settings > Statuses.
 * Business rules only look at the flags: closed (finished work: SLA stops, not counted as open, edits locked)
 * and isDefault (status given to new tickets; exactly one per organisation, never closed).
 * The code is generated from the name on creation and never changes, so filters and history stay stable.
 */
@Entity
@Table(name = "ticket_statuses", uniqueConstraints = @UniqueConstraint(name = "uk_ticket_status_org_code", columnNames = {"organisation_id", "code"}))
public class TicketStatus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;
    @Column(name = "code", nullable = false, length = 40) private String code;
    @Column(name = "name", nullable = false, length = 60) private String name;
    @Column(name = "description", length = 300) private String description;
    // Palette colour name (blue, amber, red, green, gray, purple, coral) or a #RRGGBB value
    @Column(name = "color", nullable = false, length = 20) private String color;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(name = "is_default", nullable = false) private boolean isDefault;
    @Column(name = "is_closed", nullable = false) private boolean closed;
    @Column(name = "active", nullable = false) private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation v) { organisation = v; }
    public String getCode() { return code; }
    public void setCode(String v) { code = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getColor() { return color; }
    public void setColor(String v) { color = v; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int v) { displayOrder = v; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean v) { isDefault = v; }
    public boolean isClosed() { return closed; }
    public void setClosed(boolean v) { closed = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { active = v; }
}
