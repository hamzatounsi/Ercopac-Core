package com.ercopac.ercopac_tracker.department.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"code", "organisation_id"})
})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(length = 100)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private AppUser manager;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonIgnore
    @OneToMany(mappedBy = "department")
    private List<AppUser> members = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "department")
    private List<ProjectTask> tasks = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "department")
    private List<DepartmentHoliday> holidays = new ArrayList<>();

    public Department() {}

    public Department(String code, String label, Organisation organisation) {
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
    public AppUser getManager() { return manager; }
    public void setManager(AppUser manager) { this.manager = manager; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<AppUser> getMembers() { return members; }
    public void setMembers(List<AppUser> members) { this.members = members; }
    public List<ProjectTask> getTasks() { return tasks; }
    public void setTasks(List<ProjectTask> tasks) { this.tasks = tasks; }
    public List<DepartmentHoliday> getHolidays() { return holidays; }
    public void setHolidays(List<DepartmentHoliday> holidays) { this.holidays = holidays; }
}
