package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.TaskResourceAssignment;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TaskResourceAssignmentRepository extends JpaRepository<TaskResourceAssignment, Long> {

    List<TaskResourceAssignment> findByProjectIdAndTaskIdOrderByIdAsc(Long projectId, Long taskId);

    List<TaskResourceAssignment> findByTaskIdInOrderByIdAsc(Collection<Long> taskIds);

    List<TaskResourceAssignment> findByAssignedUserIdInOrderByIdAsc(Collection<Long> assignedUserIds);

    @Query("select distinct a.task from TaskResourceAssignment a where a.assignedUserId = :userId and a.task.organisationId = :organisationId")
    List<ProjectTask> findDistinctTasksForAssignedUser(@Param("userId") Long userId, @Param("organisationId") Long organisationId);

    @Query("select distinct a.task.projectId from TaskResourceAssignment a where a.assignedUserId = :userId and a.task.organisationId = :organisationId")
    List<Long> findDistinctProjectIdsForAssignedUser(@Param("userId") Long userId, @Param("organisationId") Long organisationId);

    void deleteByProjectIdAndTaskId(Long projectId, Long taskId);
}
