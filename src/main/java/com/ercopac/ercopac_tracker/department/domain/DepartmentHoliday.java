package com.ercopac.ercopac_tracker.department.domain;

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

    public Object setMemberId(Long memberId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMemberId'");
    }
}