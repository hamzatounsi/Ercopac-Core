package com.ercopac.ercopac_tracker.milestone.repository;

import com.ercopac.ercopac_tracker.milestone.domain.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Long> {
    
    List<ProjectMilestone> findByProjectIdOrderByMilestoneDateAsc(Long projectId);
    
    @Query("SELECT pm FROM ProjectMilestone pm WHERE pm.projectId = :projectId AND pm.project.organisation.id = :organisationId ORDER BY pm.milestoneDate ASC")
    List<ProjectMilestone> findByProjectIdAndOrganisationIdOrderByMilestoneDateAsc(@Param("projectId") Long projectId, @Param("organisationId") Long organisationId);
    
    @Query("SELECT pm FROM ProjectMilestone pm " +
           "JOIN pm.project p " +
           "WHERE p.projectManagerId = :pmId " +
           "AND pm.milestoneDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pm.milestoneDate ASC")
    List<ProjectMilestone> findByProjectManagerIdAndDateRange(
        @Param("pmId") Long pmId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT pm FROM ProjectMilestone pm " +
           "JOIN pm.project p " +
           "WHERE p.organisation.id = :orgId " +
           "AND pm.milestoneDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pm.milestoneDate ASC")
    List<ProjectMilestone> findByOrganisationIdAndDateRange(
        @Param("orgId") Long orgId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    void deleteByProjectId(Long projectId);

    // ✅ THIS IS THE ONLY CORRECT METHOD FOR THE DASHBOARD
    List<ProjectMilestone> findByProjectIdInAndMilestoneDateBetween(
            List<Long> projectIds, 
            LocalDate startDate, 
            LocalDate endDate
    );
    
    // ❌ DO NOT PUT findByProjectIdInAndPlannedStartBetween HERE! It will crash the app.
}