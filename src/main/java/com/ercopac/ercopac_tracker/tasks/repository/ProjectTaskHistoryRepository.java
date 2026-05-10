package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTaskHistoryRepository extends JpaRepository<ProjectTaskHistory, Long> {

    List<ProjectTaskHistory> findByOrganisationIdAndProjectIdOrderByChangedAtDesc(
            Long organisationId,
            Long projectId
    );

    List<ProjectTaskHistory> findByOrganisationIdAndProjectIdAndTaskIdOrderByChangedAtDesc(
            Long organisationId,
            Long projectId,
            Long taskId
    );
}