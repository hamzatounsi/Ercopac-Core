package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmSalesTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CrmSalesTargetRepository extends JpaRepository<CrmSalesTarget, Long> {
    List<CrmSalesTarget> findByOrganisation_IdAndTargetYear(Long organisationId, Integer targetYear);
    Optional<CrmSalesTarget> findByOrganisation_IdAndUser_IdAndTargetYear(Long organisationId, Long userId, Integer targetYear);
}
