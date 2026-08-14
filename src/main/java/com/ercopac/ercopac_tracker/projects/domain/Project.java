package com.ercopac.ercopac_tracker.projects.domain;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.planning.domain.ProjectBaseline;
import com.ercopac.ercopac_tracker.planning.domain.ProjectCalendar;
import com.ercopac.ercopac_tracker.planning.domain.ProjectPlanning;
import com.ercopac.ercopac_tracker.planning.domain.ProjectTemplate;
import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionItem;
import com.ercopac.ercopac_tracker.projectum.change_requests.domain.ChangeRequest;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    private String shortName;
    private String portfolio;
    private String orgAssignment;
    private String country;
    private String projectType;
    private String projectPhase;
    private String priority;
    private LocalDate plannedStart;
    private LocalDate plannedEnd;
    private BigDecimal projectBudget;
    private BigDecimal totalProjectBudget;
    private Long projectManagerId;
    private String customer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customerEntity;
    private String category;
    private String riskLevel;
    private BigDecimal estimatedCost;
    private String projectManagerName;
    private String programManagerName;
    private String salesManagerName;
    private Boolean archived = false;
    private Integer progress = 0;
    @Column(name = "active_baseline_id")
    private Long activeBaselineId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectApplicationType applicationType = ProjectApplicationType.PROJECTUM;

    public ProjectApplicationType getApplicationType() {
        return applicationType;
    }
    public void setApplicationType(ProjectApplicationType applicationType) {
        this.applicationType = applicationType;
    }
    public String getCustomer() {
        return customer;
    }
    public Integer getProgress() {
        return progress;
    }
    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Long getActiveBaselineId() {
        return activeBaselineId;
    }

    public void setActiveBaselineId(Long activeBaselineId) {
        this.activeBaselineId = activeBaselineId;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Customer getCustomerEntity() { return customerEntity; }
    public void setCustomerEntity(Customer customerEntity) { this.customerEntity = customerEntity; }
    public Long getCustomerId() { return customerEntity == null ? null : customerEntity.getId(); }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getProjectManagerName() {
        return projectManagerName;
    }

    public void setProjectManagerName(String projectManagerName) {
        this.projectManagerName = projectManagerName;
    }

    public String getProgramManagerName() {
        return programManagerName;
    }

    public void setProgramManagerName(String programManagerName) {
        this.programManagerName = programManagerName;
    }

    public String getSalesManagerName() {
        return salesManagerName;
    }

    public void setSalesManagerName(String salesManagerName) {
        this.salesManagerName = salesManagerName;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    @Column(length = 1000)
    private String comment;

    @JsonIgnore
    @OneToOne(mappedBy = "project")
    private ProjectPlanning planning;

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<ProjectCalendar> calendars = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<ProjectBaseline> baselines = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<ProjectTemplate> templates = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<FinanceEntry> financeEntries = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<ForecastEntry> forecastEntries = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<RiskItem> risks = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<ChangeRequest> changeRequests = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<ActionItem> actionItems = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    private List<CrmOpportunity> crmOpportunities = new ArrayList<>();

    public Project() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public String getOrgAssignment() {
        return orgAssignment;
    }

    public void setOrgAssignment(String orgAssignment) {
        this.orgAssignment = orgAssignment;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getProjectPhase() {
        return projectPhase;
    }

    public void setProjectPhase(String projectPhase) {
        this.projectPhase = projectPhase;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getPlannedStart() {
        return plannedStart;
    }

    public void setPlannedStart(LocalDate plannedStart) {
        this.plannedStart = plannedStart;
    }

    public LocalDate getPlannedEnd() {
        return plannedEnd;
    }

    public void setPlannedEnd(LocalDate plannedEnd) {
        this.plannedEnd = plannedEnd;
    }

    public BigDecimal getProjectBudget() {
        return projectBudget;
    }

    public void setProjectBudget(BigDecimal projectBudget) {
        this.projectBudget = projectBudget;
    }

    public BigDecimal getTotalProjectBudget() {
        return totalProjectBudget;
    }

    public void setTotalProjectBudget(BigDecimal totalProjectBudget) {
        this.totalProjectBudget = totalProjectBudget;
    }

    public Long getProjectManagerId() {
        return projectManagerId;
    }

    public void setProjectManagerId(Long projectManagerId) {
        this.projectManagerId = projectManagerId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ProjectPlanning getPlanning() { return planning; }
    public void setPlanning(ProjectPlanning planning) { this.planning = planning; }
    public List<ProjectCalendar> getCalendars() { return calendars; }
    public void setCalendars(List<ProjectCalendar> calendars) { this.calendars = calendars; }
    public List<ProjectBaseline> getBaselines() { return baselines; }
    public void setBaselines(List<ProjectBaseline> baselines) { this.baselines = baselines; }
    public List<ProjectTemplate> getTemplates() { return templates; }
    public void setTemplates(List<ProjectTemplate> templates) { this.templates = templates; }
    public List<FinanceEntry> getFinanceEntries() { return financeEntries; }
    public void setFinanceEntries(List<FinanceEntry> financeEntries) { this.financeEntries = financeEntries; }
    public List<ForecastEntry> getForecastEntries() { return forecastEntries; }
    public void setForecastEntries(List<ForecastEntry> forecastEntries) { this.forecastEntries = forecastEntries; }
    public List<RiskItem> getRisks() { return risks; }
    public void setRisks(List<RiskItem> risks) { this.risks = risks; }
    public List<ChangeRequest> getChangeRequests() { return changeRequests; }
    public void setChangeRequests(List<ChangeRequest> changeRequests) { this.changeRequests = changeRequests; }
    public List<ActionItem> getActionItems() { return actionItems; }
    public void setActionItems(List<ActionItem> actionItems) { this.actionItems = actionItems; }
    public List<CrmOpportunity> getCrmOpportunities() { return crmOpportunities; }
    public void setCrmOpportunities(List<CrmOpportunity> crmOpportunities) { this.crmOpportunities = crmOpportunities; }
}
