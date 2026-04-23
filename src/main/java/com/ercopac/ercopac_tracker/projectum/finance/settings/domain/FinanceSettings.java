package com.ercopac.ercopac_tracker.projectum.finance.settings.domain;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "finance_settings")
public class FinanceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false, unique = true)
    private Organisation organisation;

    @Column(name = "default_hourly_rate", precision = 18, scale = 2, nullable = false)
    private BigDecimal defaultHourlyRate = BigDecimal.valueOf(65);

    public Long getId() {
        return id;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public BigDecimal getDefaultHourlyRate() {
        return defaultHourlyRate;
    }

    public void setDefaultHourlyRate(BigDecimal defaultHourlyRate) {
        this.defaultHourlyRate = defaultHourlyRate;
    }
}