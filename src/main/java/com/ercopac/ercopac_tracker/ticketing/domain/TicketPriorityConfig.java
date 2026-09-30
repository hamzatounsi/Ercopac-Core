package com.ercopac.ercopac_tracker.ticketing.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_priority_configs")
public class TicketPriorityConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private TicketPriority priority;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(name = "first_response_sla_hours", nullable = false)
    private int firstResponseSlaHours;

    @Column(name = "resolution_sla_hours", nullable = false)
    private int resolutionSlaHours;

    @Column(nullable = false)
    private boolean active = true;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public int getFirstResponseSlaHours() { return firstResponseSlaHours; }
    public void setFirstResponseSlaHours(int hours) { this.firstResponseSlaHours = hours; }
    
    public int getResolutionSlaHours() { return resolutionSlaHours; }
    public void setResolutionSlaHours(int hours) { this.resolutionSlaHours = hours; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}