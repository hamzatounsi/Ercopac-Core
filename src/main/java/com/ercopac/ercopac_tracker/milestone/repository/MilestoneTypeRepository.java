package com.ercopac.ercopac_tracker.milestone.repository;

import com.ercopac.ercopac_tracker.milestone.domain.MilestoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MilestoneTypeRepository extends JpaRepository<MilestoneType, Long> {
    List<MilestoneType> findByProjectIdAndOrganisation_IdAndActiveTrueOrderByCodeAsc(Long projectId, Long organisationId);
    Optional<MilestoneType> findByIdAndProjectIdAndOrganisation_Id(Long id, Long projectId, Long organisationId);
    boolean existsByProjectIdAndLabelIgnoreCase(Long projectId, String label);
    Optional<MilestoneType> findByProjectIdAndLabelIgnoreCase(Long projectId, String label);
}
