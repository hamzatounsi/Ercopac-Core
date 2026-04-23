package com.ercopac.ercopac_tracker.projectum.finance.settings.repository;

import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinanceSettingsRepository extends JpaRepository<FinanceSettings, Long> {
    Optional<FinanceSettings> findByOrganisationId(Long organisationId);
}