package com.ercopac.ercopac_tracker.user;

import com.ercopac.ercopac_tracker.department.domain.Department;
import com.ercopac.ercopac_tracker.department.domain.DepartmentHoliday;
import com.ercopac.ercopac_tracker.crm.domain.CrmActivity;
import com.ercopac.ercopac_tracker.crm.domain.CrmLead;
import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionAssignee;
import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionItem;
import com.ercopac.ercopac_tracker.projectum.change_requests.domain.ChangeRequest;
import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskItem;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskResourceAssignment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @Column(name = "employee_code", length = 40)
    private String employeeCode;

    @Column(name = "department_code", length = 30)
    private String departmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "job_title", length = 80)
    private String jobTitle;

 // Add this:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_type_id")
    private ResourceType resourceType;

    @Column(name = "seniority", length = 30)
    private String seniority;

    @Column(name = "hours_per_day")
    private Integer hoursPerDay = 8;

    @Column(name = "days_per_week")
    private Integer daysPerWeek = 5;

    @Column(name = "workdays", length = 30)
    private String workdays = "MON-FRI";

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "internal_user", nullable = false)
    private boolean internalUser = true;

    @Column(name = "default_rate", precision = 12, scale = 2)
    private BigDecimal defaultRate;

    @Column(name = "rate_type", length = 20)
    private String rateType;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    private Boolean emailNotificationsEnabled = true;

    @JsonIgnore
    @OneToMany(mappedBy = "manager")
    private List<Department> managedDepartments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assignedUser")
    private List<ProjectTask> assignedTasks = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assignedUser")
    private List<TaskResourceAssignment> taskResourceAssignments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "ownerUser")
    private List<RiskItem> ownedRisks = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "owner")
    private List<ActionItem> ownedActions = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assigneeUser")
    private List<ActionAssignee> actionAssignments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "requester")
    private List<ChangeRequest> requestedChangeRequests = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "approver")
    private List<ChangeRequest> approvedChangeRequests = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<DepartmentHoliday> holidays = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "owner")
    private List<CrmLead> ownedCrmLeads = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "owner")
    private List<CrmOpportunity> ownedCrmOpportunities = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<CrmActivity> crmActivities = new ArrayList<>();

    public Boolean getEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public AppUser() {
    }

    public AppUser(String fullName, String email, String passwordHash, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
        this.internalUser = true;
        this.hoursPerDay = 8;
        this.daysPerWeek = 5;
        this.workdays = "MON-FRI";
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    public String getSeniority() {
        return seniority;
    }

    public void setSeniority(String seniority) {
        this.seniority = seniority;
    }

    public Integer getHoursPerDay() {
        return hoursPerDay;
    }

    public void setHoursPerDay(Integer hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    public Integer getDaysPerWeek() {
        return daysPerWeek;
    }

    public void setDaysPerWeek(Integer daysPerWeek) {
        this.daysPerWeek = daysPerWeek;
    }

    public String getWorkdays() {
        return workdays;
    }

    public void setWorkdays(String workdays) {
        this.workdays = workdays;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isInternalUser() {
        return internalUser;
    }

    public void setInternalUser(boolean internalUser) {
        this.internalUser = internalUser;
    }

    public BigDecimal getDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(BigDecimal defaultRate) {
        this.defaultRate = defaultRate;
    }

    public String getRateType() {
        return rateType;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUsername() {
        return email;
    }

    public List<Department> getManagedDepartments() { return managedDepartments; }
    public void setManagedDepartments(List<Department> managedDepartments) { this.managedDepartments = managedDepartments; }
    public List<ProjectTask> getAssignedTasks() { return assignedTasks; }
    public void setAssignedTasks(List<ProjectTask> assignedTasks) { this.assignedTasks = assignedTasks; }
    public List<TaskResourceAssignment> getTaskResourceAssignments() { return taskResourceAssignments; }
    public void setTaskResourceAssignments(List<TaskResourceAssignment> taskResourceAssignments) { this.taskResourceAssignments = taskResourceAssignments; }
    public List<RiskItem> getOwnedRisks() { return ownedRisks; }
    public void setOwnedRisks(List<RiskItem> ownedRisks) { this.ownedRisks = ownedRisks; }
    public List<ActionItem> getOwnedActions() { return ownedActions; }
    public void setOwnedActions(List<ActionItem> ownedActions) { this.ownedActions = ownedActions; }
    public List<ActionAssignee> getActionAssignments() { return actionAssignments; }
    public void setActionAssignments(List<ActionAssignee> actionAssignments) { this.actionAssignments = actionAssignments; }
    public List<ChangeRequest> getRequestedChangeRequests() { return requestedChangeRequests; }
    public void setRequestedChangeRequests(List<ChangeRequest> requestedChangeRequests) { this.requestedChangeRequests = requestedChangeRequests; }
    public List<ChangeRequest> getApprovedChangeRequests() { return approvedChangeRequests; }
    public void setApprovedChangeRequests(List<ChangeRequest> approvedChangeRequests) { this.approvedChangeRequests = approvedChangeRequests; }
    public List<DepartmentHoliday> getHolidays() { return holidays; }
    public void setHolidays(List<DepartmentHoliday> holidays) { this.holidays = holidays; }
    public List<CrmLead> getOwnedCrmLeads() { return ownedCrmLeads; }
    public void setOwnedCrmLeads(List<CrmLead> ownedCrmLeads) { this.ownedCrmLeads = ownedCrmLeads; }
    public List<CrmOpportunity> getOwnedCrmOpportunities() { return ownedCrmOpportunities; }
    public void setOwnedCrmOpportunities(List<CrmOpportunity> ownedCrmOpportunities) { this.ownedCrmOpportunities = ownedCrmOpportunities; }
    public List<CrmActivity> getCrmActivities() { return crmActivities; }
    public void setCrmActivities(List<CrmActivity> crmActivities) { this.crmActivities = crmActivities; }
}
