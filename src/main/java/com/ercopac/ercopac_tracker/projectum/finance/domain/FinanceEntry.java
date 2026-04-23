package com.ercopac.ercopac_tracker.projectum.finance.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsRowType;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "finance_entries")
public class FinanceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_name")
    private String ownerName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String wbsCode;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private Integer level;

    @Column(precision = 18, scale = 2)
    private BigDecimal sales = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal commitment = BigDecimal.ZERO;

    @Column(name = "actual_cost", precision = 18, scale = 2)
    private BigDecimal actualCost = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal forecast = BigDecimal.ZERO;

    @Column(name = "owner_key", length = 100)
    private String ownerKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "row_type", length = 20)
    private FinanceWbsRowType rowType;

    @Column(name = "hour_rate", precision = 18, scale = 2)
    private BigDecimal hourRate;

    @Column(name = "is_summary", nullable = false)
    private Boolean isSummary = false;

    public String getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    public FinanceWbsRowType getRowType() {
        return rowType;
    }

    public void setRowType(FinanceWbsRowType rowType) {
        this.rowType = rowType;
    }

    public BigDecimal getHourRate() {
        return hourRate;
    }

    public void setHourRate(BigDecimal hourRate) {
        this.hourRate = hourRate;
    }

    public Boolean getIsSummary() {
        return isSummary;
    }

    public void setIsSummary(Boolean isSummary) {
        this.isSummary = isSummary;
    }

    public FinanceEntry() {
    }

    public Long getId() {
        return id;
    }

    public String getOwnerName() {
    return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public void setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public BigDecimal getSales() {
        return sales;
    }

    public void setSales(BigDecimal sales) {
        this.sales = sales;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getCommitment() {
        return commitment;
    }

    public void setCommitment(BigDecimal commitment) {
        this.commitment = commitment;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public BigDecimal getForecast() {
        return forecast;
    }

    public void setForecast(BigDecimal forecast) {
        this.forecast = forecast;
    }
}