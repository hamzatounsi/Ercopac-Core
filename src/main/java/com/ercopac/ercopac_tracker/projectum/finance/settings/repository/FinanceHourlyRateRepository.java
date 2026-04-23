package com.ercopac.ercopac_tracker.projectum.finance.settings.repository;

import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceHourlyRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceHourlyRateRepository extends JpaRepository<FinanceHourlyRate, Long> {
    List<FinanceHourlyRate> findAllByOrganisationIdOrderByResourceTypeAsc(Long organisationId);
    void deleteAllByOrganisationId(Long organisationId);
}