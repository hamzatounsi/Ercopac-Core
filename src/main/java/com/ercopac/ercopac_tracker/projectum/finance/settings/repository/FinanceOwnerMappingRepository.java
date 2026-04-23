package com.ercopac.ercopac_tracker.projectum.finance.settings.repository;

import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceOwnerMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceOwnerMappingRepository extends JpaRepository<FinanceOwnerMapping, Long> {
    List<FinanceOwnerMapping> findAllByOrganisationIdOrderByOwnerKeyAsc(Long organisationId);
    void deleteAllByOrganisationId(Long organisationId);
}