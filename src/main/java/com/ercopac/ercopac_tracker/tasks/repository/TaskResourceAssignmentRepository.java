package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.TaskResourceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskResourceAssignmentRepository extends JpaRepository<TaskResourceAssignment, Long> {

    List<TaskResourceAssignment> findByProjectIdAndTaskIdOrderByIdAsc(Long projectId, Long taskId);

    void deleteByProjectIdAndTaskId(Long projectId, Long taskId);
}