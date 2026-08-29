package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CrmIndustryRepository extends JpaRepository<CrmIndustry, Long> {
    List<CrmIndustry> findByOrganisation_IdOrderByNameAsc(Long organisationId);
    List<CrmIndustry> findByOrganisation_IdAndActiveTrueOrderByNameAsc(Long organisationId);
    Optional<CrmIndustry> findByIdAndOrganisation_Id(Long id, Long organisationId);
    Optional<CrmIndustry> findByOrganisation_IdAndNameIgnoreCase(Long organisationId, String name);
}
