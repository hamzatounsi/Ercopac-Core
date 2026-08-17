package com.ercopac.ercopac_tracker.projectum.finance.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "finance_entries")
public class FinanceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "wbs_code", nullable = false, length = 100)
    private String wbsCode;

    @Column(name = "resource_type_code", length = 40)
    private String resourceTypeCode;

    @Column(name = "level")
    private Integer level;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "row_type", length = 30)
    private String rowType;

    public String getLinkedScheduleWbs() { return linkedScheduleWbs; }
    public void setLinkedScheduleWbs(String linkedScheduleWbs) { this.linkedScheduleWbs = linkedScheduleWbs; }

    @Column(name = "linked_schedule_wbs", length = 100)
    private String linkedScheduleWbs;

    @Column(name = "is_summary", nullable = false)
    private Boolean isSummary = false;

    // ✅ NOUVEAU : Pour préserver l'ordre du template WBS
    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "sales", precision = 18, scale = 2)
    private BigDecimal sales = BigDecimal.ZERO;

    @Column(name = "budget", precision = 18, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    @Column(name = "cost_reserve", precision = 18, scale = 2)
    private BigDecimal costReserve = BigDecimal.ZERO;

    @Column(name = "updated_budget", precision = 18, scale = 2)
    private BigDecimal updatedBudget = BigDecimal.ZERO;

    @Column(name = "commitment", precision = 18, scale = 2)
    private BigDecimal commitment = BigDecimal.ZERO;

    @Column(name = "actual_cost", precision = 18, scale = 2)
    private BigDecimal actualCost = BigDecimal.ZERO;

    @Column(name = "forecast", precision = 18, scale = 2)
    private BigDecimal forecast = BigDecimal.ZERO;

    @Column(name = "owner_name", length = 150)
    private String ownerName;

    @Column(name = "hour_rate", precision = 10, scale = 2)
    private BigDecimal hourRate;

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }

    public String getResourceTypeCode() { return resourceTypeCode; }
    public void setResourceTypeCode(String resourceTypeCode) { this.resourceTypeCode = resourceTypeCode; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRowType() { return rowType; }
    public void setRowType(String rowType) { this.rowType = rowType; }

    public String getLinkedScheduleWbs() { return linkedScheduleWbs; }
    public void setLinkedScheduleWbs(String linkedScheduleWbs) { this.linkedScheduleWbs = linkedScheduleWbs; }

    public Boolean getIsSummary() { return isSummary; }
    public void setIsSummary(Boolean isSummary) { this.isSummary = isSummary; }

    // ✅ GETTERS/SETTERS POUR DISPLAY ORDER
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public BigDecimal getSales() { return sales; }
    public void setSales(BigDecimal sales) { this.sales = sales; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public BigDecimal getCostReserve() { return costReserve; }
    public void setCostReserve(BigDecimal costReserve) { this.costReserve = costReserve; }

    public BigDecimal getUpdatedBudget() { return updatedBudget; }
    public void setUpdatedBudget(BigDecimal updatedBudget) { this.updatedBudget = updatedBudget; }

    public BigDecimal getCommitment() { return commitment; }
    public void setCommitment(BigDecimal commitment) { this.commitment = commitment; }

    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }

    public BigDecimal getForecast() { return forecast; }
    public void setForecast(BigDecimal forecast) { this.forecast = forecast; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public BigDecimal getHourRate() { return hourRate; }
    public void setHourRate(BigDecimal hourRate) { this.hourRate = hourRate; }
}
