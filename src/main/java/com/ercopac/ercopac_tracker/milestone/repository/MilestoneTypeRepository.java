package com.ercopac.ercopac_tracker.milestone.repository;

import com.ercopac.ercopac_tracker.milestone.domain.MilestoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MilestoneTypeRepository extends JpaRepository<MilestoneType, Long> {
    List<MilestoneType> findByOrganisationIdAndActiveTrueOrderByCodeAsc(Long organisationId);
    List<MilestoneType> findByActiveTrueOrderByCodeAsc();
}