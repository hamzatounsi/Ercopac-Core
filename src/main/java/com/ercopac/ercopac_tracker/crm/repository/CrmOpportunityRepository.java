package com.ercopac.ercopac_tracker.crm.repository;
 
import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
 
public interface CrmOpportunityRepository extends JpaRepository<CrmOpportunity, Long> {
 
    List<CrmOpportunity> findByOrganisation_IdOrderByCreatedAtDesc(Long orgId);
 
    List<CrmOpportunity> findByOrganisation_IdAndWonFalseAndLostFalseOrderByCreatedAtDesc(
            Long orgId);
 
    List<CrmOpportunity> findByOrganisation_IdAndOwner_IdOrderByCreatedAtDesc(
            Long orgId, Long ownerId);
 
    // Closing this month
    List<CrmOpportunity> findByOrganisation_IdAndClosingDateBetweenOrderByClosingDateAsc(
            Long orgId, LocalDate from, LocalDate to);
 
    // Count open opportunities
    long countByOrganisation_IdAndWonFalseAndLostFalse(Long orgId);
 
    // Pipeline value (sum of all open)
    @Query("SELECT SUM(o.value) FROM CrmOpportunity o " +
           "WHERE o.organisation.id = :orgId " +
           "AND o.won = false AND o.lost = false AND o.value IS NOT NULL")
    java.math.BigDecimal sumPipelineValue(@Param("orgId") Long orgId);
 
    // Won this month
    @Query("SELECT COUNT(o) FROM CrmOpportunity o " +
           "WHERE o.organisation.id = :orgId AND o.won = true " +
           "AND o.updatedAt >= :from")
    long countWonSince(@Param("orgId") Long orgId,
                       @Param("from") java.time.LocalDateTime from);
 
    // Group by stage for pipeline kanban
    @Query("SELECT o.stage, o FROM CrmOpportunity o " +
           "WHERE o.organisation.id = :orgId " +
           "AND o.won = false AND o.lost = false " +
           "ORDER BY o.stage.displayOrder ASC")
    List<CrmOpportunity> findOpenByOrgGroupedByStage(@Param("orgId") Long orgId);
}