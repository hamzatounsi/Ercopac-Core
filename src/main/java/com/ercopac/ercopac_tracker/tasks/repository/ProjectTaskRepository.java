package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    List<ProjectTask> findByProjectIdOrderByDisplayOrderAscIdAsc(Long projectId);
    List<ProjectTask> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);
}