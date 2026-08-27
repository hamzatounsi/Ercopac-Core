package com.ercopac.ercopac_tracker.crm.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_sales_targets", uniqueConstraints =
        @UniqueConstraint(name = "uq_crm_target_org_user_year", columnNames = {"organisation_id", "user_id", "target_year"}))
public class CrmSalesTarget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "organisation_id", nullable = false) private Organisation organisation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private AppUser user;
    @Column(name = "target_year", nullable = false) private Integer targetYear;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal amount = BigDecimal.ZERO;
    @Column(nullable = false, length = 10) private String currency = "EUR";
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    @PreUpdate void updated() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation value) { organisation = value; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser value) { user = value; }
    public Integer getTargetYear() { return targetYear; }
    public void setTargetYear(Integer value) { targetYear = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getCurrency() { return currency; }
    public void setCurrency(String value) { currency = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
