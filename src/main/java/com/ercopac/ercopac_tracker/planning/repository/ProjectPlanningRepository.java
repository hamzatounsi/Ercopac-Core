package com.ercopac.ercopac_tracker.planning.repository;

import com.ercopac.ercopac_tracker.planning.domain.ProjectPlanning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectPlanningRepository extends JpaRepository<ProjectPlanning, Long> {

    Optional<ProjectPlanning> findByProjectId(Long projectId);
    boolean existsByProjectId(Long projectId);
}