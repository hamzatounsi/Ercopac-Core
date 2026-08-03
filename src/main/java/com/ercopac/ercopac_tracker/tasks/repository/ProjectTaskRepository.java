package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    List<ProjectTask> findByProjectIdOrderByDisplayOrderAscIdAsc(Long projectId);
    List<ProjectTask> findByProjectIdAndOrganisationIdOrderByDisplayOrderAscIdAsc(Long projectId, Long organisationId);
    List<ProjectTask> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);
    Optional<ProjectTask> findByProjectIdAndWbsCode(Long projectId, String wbsCode);
    boolean existsByProjectIdAndWbsCode(Long projectId, String wbsCode);
    List<ProjectTask> findByAssignedUser_Id(Long userId);
    List<ProjectTask> findByAssignedUser_IdAndOrganisationId(Long userId, Long organisationId);
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
}
