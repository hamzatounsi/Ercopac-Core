package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmSupplyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CrmSupplyCategoryRepository extends JpaRepository<CrmSupplyCategory, Long> {
    List<CrmSupplyCategory> findByOrganisation_IdAndActiveTrueOrderByDisplayOrderAscNameAsc(Long organisationId);
    Optional<CrmSupplyCategory> findByIdAndOrganisation_Id(Long id, Long organisationId);
    long countByOrganisation_Id(Long organisationId);
    boolean existsByOrganisation_IdAndNameIgnoreCase(Long organisationId, String name);
}
