package com.ercopac.ercopac_tracker.department.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "department_holidays")
public class DepartmentHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organisation_id", nullable = false)
    private Long organisationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false, insertable = false, updatable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private AppUser member;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_by")
    private Long createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private AppUser createdByUser;

    public DepartmentHoliday() {
    }

    public DepartmentHoliday(Long id, Long organisationId, AppUser member, LocalDate fromDate, LocalDate toDate, String note, Long createdBy) {
        this.id = id;
        this.organisationId = organisationId;
        this.member = member;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.note = note;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public AppUser getMember() {
        return member;
    }

    public void setMember(AppUser member) {
        this.member = member;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public AppUser getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(AppUser createdByUser) { this.createdByUser = createdByUser; }

    public Long getMemberId() {
        return member == null ? null : member.getId();
    }
}
