package com.ercopac.ercopac_tracker.ticketing.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "equipment_types")
public class EquipmentType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // ex: "Server", "Network switch"

    @Column(length = 10)
    private String code; // ex: "eq1", "eq2"

    @Column(length = 20)
    private String icon; // emoji ou classe CSS

    @Column(nullable = false)
    private boolean active = true;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}