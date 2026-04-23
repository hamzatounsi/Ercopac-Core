package com.ercopac.ercopac_tracker.projectum.change_requests.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "change_requests")
public class ChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 30)
    private String status; // open, submitted, accepted, refused, cancelled

    private LocalDate requestDate;

    @Column(precision = 18, scale = 2)
    private BigDecimal valueAmount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal costAmount = BigDecimal.ZERO;

    @Column(length = 120)
    private String owner;

    @Column(length = 2000)
    private String note;

    @OneToMany(mappedBy = "changeRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChangeRequestAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "changeRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ChangeRequestHistoryEntry> history = new ArrayList<>();

    public ChangeRequest() {}

    public Long getId() { return id; }

    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public BigDecimal getValueAmount() { return valueAmount; }
    public void setValueAmount(BigDecimal valueAmount) { this.valueAmount = valueAmount; }

    public BigDecimal getCostAmount() { return costAmount; }
    public void setCostAmount(BigDecimal costAmount) { this.costAmount = costAmount; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<ChangeRequestAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<ChangeRequestAttachment> attachments) { this.attachments = attachments; }

    public List<ChangeRequestHistoryEntry> getHistory() { return history; }
    public void setHistory(List<ChangeRequestHistoryEntry> history) { this.history = history; }
}