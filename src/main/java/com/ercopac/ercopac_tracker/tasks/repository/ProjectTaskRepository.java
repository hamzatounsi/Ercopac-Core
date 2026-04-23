package com.ercopac.ercopac_tracker.tasks.repository;

import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    List<ProjectTask> findByProjectIdOrderByDisplayOrderAscIdAsc(Long projectId);
    List<ProjectTask> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);
    Optional<ProjectTask> findByProjectIdAndWbsCode(Long projectId, String wbsCode);
    boolean existsByProjectIdAndWbsCode(Long projectId, String wbsCode);
    List<ProjectTask> findByAssignedUser_Id(Long userId);
    List<ProjectTask> findByProjectIdOrderByDisplayOrderAsc(Long projectId);
    List<ProjectTask> findByAssignedUser_DepartmentCodeAndAssignedUser_Organisation_Id(
    String departmentCode,
    Long organisationId
    );
    void deleteByProjectId(Long projectId);
}