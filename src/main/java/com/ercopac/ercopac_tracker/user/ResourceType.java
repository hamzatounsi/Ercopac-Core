package com.ercopac.ercopac_tracker.user;


import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_types", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"code", "organisation_id"})
})
public class ResourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(length = 100)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ResourceType() {}

    public ResourceType(String code, String label, Organisation organisation) {
        this.code = code;
        this.label = label;
        this.organisation = organisation;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}