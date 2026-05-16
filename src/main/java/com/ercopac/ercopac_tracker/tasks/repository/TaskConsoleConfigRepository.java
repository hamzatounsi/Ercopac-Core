package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.TaskConsoleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskConsoleConfigRepository extends JpaRepository<TaskConsoleConfig, Long> {
    Optional<TaskConsoleConfig> findByOrganisationIdAndProjectIdAndTaskId(Long organisationId, Long projectId, Long taskId);
}