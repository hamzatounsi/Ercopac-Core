package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.TaskConsoleLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskConsoleLogRepository extends JpaRepository<TaskConsoleLog, Long> {
    List<TaskConsoleLog> findByOrganisationIdAndProjectIdAndTaskIdOrderByCreatedAtDesc(Long organisationId, Long projectId, Long taskId);
    void deleteByOrganisationIdAndProjectIdAndTaskId(Long organisationId, Long projectId, Long taskId);
}