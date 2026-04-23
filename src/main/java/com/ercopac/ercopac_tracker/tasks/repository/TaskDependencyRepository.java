package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    List<TaskDependency> findByProjectId(Long projectId);

    List<TaskDependency> findByPredecessorTaskId(Long taskId);

    List<TaskDependency> findBySuccessorTaskId(Long successorTaskId);

    List<TaskDependency> findByProjectIdAndPredecessorTaskId(Long projectId, Long predecessorTaskId);

    List<TaskDependency> findByProjectIdAndSuccessorTaskId(Long projectId, Long successorTaskId);

    void deleteByProjectId(Long projectId);

    void deleteByPredecessorTaskId(Long taskId);

    void deleteBySuccessorTaskId(Long taskId);
}