package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskResourceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    List<ProjectTask> findByProjectIdOrderByDisplayOrderAscIdAsc(Long projectId);
    List<ProjectTask> findByProjectIdAndOrganisationIdOrderByDisplayOrderAscIdAsc(Long projectId, Long organisationId);
    List<ProjectTask> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);
    boolean existsByProjectIdAndSourceTemplateId(Long projectId, Long sourceTemplateId);
    Optional<ProjectTask> findByProjectIdAndWbsCode(Long projectId, String wbsCode);
    boolean existsByProjectIdAndWbsCode(Long projectId, String wbsCode);
    List<ProjectTask> findByAssignedUser_Id(Long userId);
    List<ProjectTask> findByAssignedUser_IdAndOrganisationId(Long userId, Long organisationId);
    long countByProjectIdAndAssignedUser_IdAndOrganisationId(Long projectId, Long userId, Long organisationId);

    @Query("select count(t) from ProjectTask t where t.projectId = :projectId and t.organisationId = :organisationId " +
           "and (t.assignedUser.id = :userId or exists (select a.id from TaskResourceAssignment a where a.task.id = t.id and a.assignedUserId = :userId))")
    long countAssignedToUserInProject(@Param("projectId") Long projectId, @Param("userId") Long userId,
                                      @Param("organisationId") Long organisationId);

    @Query("select distinct t.projectId from ProjectTask t where t.assignedUser.id = :userId and t.organisationId = :organisationId")
    List<Long> findDistinctProjectIdsByAssignedUserIdAndOrganisationId(
        @Param("userId") Long userId,
        @Param("organisationId") Long organisationId);
    List<ProjectTask> findByAssignedUser_IdInAndOrganisationId(Collection<Long> userIds, Long organisationId);
    List<ProjectTask> findByDepartmentCodeAndOrganisationIdOrderByDisplayOrderAscIdAsc(String departmentCode, Long organisationId);
    List<ProjectTask> findByDepartment_IdAndOrganisationIdOrderByDisplayOrderAscIdAsc(Long departmentId, Long organisationId);
    long countByDepartment_IdAndOrganisationId(Long departmentId, Long organisationId);
    long countByDepartmentCodeAndOrganisationId(String departmentCode, Long organisationId);
    List<ProjectTask> findByProjectIdOrderByDisplayOrderAsc(Long projectId);
    List<ProjectTask> findByAssignedUser_DepartmentCodeAndAssignedUser_Organisation_Id(
        String departmentCode,
        Long organisationId
    );
    void deleteByProjectId(Long projectId);
    void flush();
    
    @Query(value = """
        UPDATE project_tasks pt
        SET duration_days = (
            SELECT COALESCE(SUM(c.duration_days), 0)
            FROM project_tasks c
            WHERE c.parent_id = pt.id
        )
        WHERE pt.project_id = :projectId
        AND pt.task_type = 'SUMMARY'
        """, nativeQuery = true)
    @Modifying
    void updateSummaryDurations(@Param("projectId") Long projectId);

    // ✅ NEW METHOD: Fetches tasks marked as MILESTONE within a specific date range, 
    // and eagerly loads the milestoneType to get the color and letter code.
    @Query("SELECT t FROM ProjectTask t JOIN FETCH t.milestoneType " +
           "WHERE t.taskType = :taskType " +
           "AND t.projectId IN :projectIds " +
           "AND t.baselineStart BETWEEN :startDate AND :endDate")
    List<ProjectTask> findByTaskTypeAndProjectIdInAndBaselineStartBetween(
        @Param("taskType") String taskType,
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    // ✅ THE BULLETPROOF QUERY
    @Query("SELECT t FROM ProjectTask t LEFT JOIN FETCH t.milestoneType " +
           "WHERE UPPER(t.taskType) = 'MILESTONE' " +
           "AND t.projectId IN :projectIds " +
           "AND (t.baselineStart BETWEEN :startDate AND :endDate " +
           "     OR t.plannedStart BETWEEN :startDate AND :endDate)")
    List<ProjectTask> findMilestoneTasksByDateRange(
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
