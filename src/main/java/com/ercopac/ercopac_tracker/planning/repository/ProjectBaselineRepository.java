package com.ercopac.ercopac_tracker.planning.repository;

import com.ercopac.ercopac_tracker.planning.domain.ProjectBaseline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectBaselineRepository extends JpaRepository<ProjectBaseline, Long> {

    List<ProjectBaseline> findByProjectIdAndOrganisationIdOrderByCreatedAtDesc(Long projectId, Long organisationId);

    Optional<ProjectBaseline> findByIdAndProjectIdAndOrganisationId(Long id, Long projectId, Long organisationId);
}