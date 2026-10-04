package com.ercopac.ercopac_tracker.ticketing.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_status_configs")
public class TicketStatusConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private TicketStatus status;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(length = 250)
    private String description;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sla_trigger", length = 100)
    private String slaTrigger; // ex: "First response SLA starts"

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getSlaTrigger() { return slaTrigger; }
    public void setSlaTrigger(String slaTrigger) { this.slaTrigger = slaTrigger; }
}