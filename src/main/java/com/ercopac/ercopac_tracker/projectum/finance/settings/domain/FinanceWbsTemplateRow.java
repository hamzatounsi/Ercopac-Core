package com.ercopac.ercopac_tracker.projectum.finance.settings.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "finance_wbs_template_rows")
public class FinanceWbsTemplateRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "level_no", nullable = false)
    private Integer level;

    @Column(name = "code_template", nullable = false, length = 100)
    private String codeTemplate;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinanceWbsRowType type;

    @Column(name = "owner_key", length = 100)
    private String ownerKey;

    @Column(name = "hour_rate", precision = 18, scale = 2)
    private BigDecimal hourRate;

    public Long getId() {
        return id;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getCodeTemplate() {
        return codeTemplate;
    }

    public void setCodeTemplate(String codeTemplate) {
        this.codeTemplate = codeTemplate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FinanceWbsRowType getType() {
        return type;
    }

    public void setType(FinanceWbsRowType type) {
        this.type = type;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    public BigDecimal getHourRate() {
        return hourRate;
    }

    public void setHourRate(BigDecimal hourRate) {
        this.hourRate = hourRate;
    }
}