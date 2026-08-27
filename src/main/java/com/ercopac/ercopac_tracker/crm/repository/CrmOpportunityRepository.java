package com.ercopac.ercopac_tracker.crm.repository;
 
import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
 
public interface CrmOpportunityRepository extends JpaRepository<CrmOpportunity, Long> {
 
    List<CrmOpportunity> findByOrganisation_IdOrderByCreatedAtDesc(Long orgId);

    List<CrmOpportunity> findByOrganisation_IdAndOpportunityTypeIgnoreCaseOrderByCreatedAtDesc(Long orgId, String opportunityType);
 
    List<CrmOpportunity> findByOrganisation_IdAndWonFalseAndLostFalseOrderByCreatedAtDesc(
            Long orgId);
 
    List<CrmOpportunity> findByOrganisation_IdAndOwner_IdOrderByCreatedAtDesc(
            Long orgId, Long ownerId);

    Optional<CrmOpportunity> findByIdAndOrganisation_Id(Long id, Long orgId);

    List<CrmOpportunity> findByOrganisation_IdAndAccount_IdOrderByCreatedAtDesc(Long orgId, Long accountId);

    List<CrmOpportunity> findByOrganisation_IdAndLead_IdOrderByCreatedAtDesc(Long orgId, Long leadId);

    @Query("SELECT o FROM CrmOpportunity o WHERE o.organisation.id = :orgId " +
           "AND (:ownerId IS NULL OR o.owner.id = :ownerId) " +
           "AND (:accountId IS NULL OR o.account.id = :accountId) " +
           "AND (:leadId IS NULL OR o.lead.id = :leadId) " +
           "AND (:stageId IS NULL OR o.stage.id = :stageId) " +
           "ORDER BY o.createdAt DESC")
    List<CrmOpportunity> findFiltered(@Param("orgId") Long orgId,
                                      @Param("ownerId") Long ownerId,
                                      @Param("accountId") Long accountId,
                                      @Param("leadId") Long leadId,
                                      @Param("stageId") Long stageId);

    long countByOrganisation_IdAndAccount_Id(Long orgId, Long accountId);

    boolean existsByOrganisation_IdAndStage_Id(Long orgId, Long stageId);

    boolean existsByOrganisation_IdAndSupplyCategory_Id(Long orgId, Long categoryId);

    @Query("select coalesce(sum(o.value),0) from CrmOpportunity o where o.organisation.id=:orgId and o.account.id=:accountId")
    java.math.BigDecimal sumValueByAccount(@Param("orgId") Long orgId, @Param("accountId") Long accountId);
 
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

    @Query("SELECT o.stage.id, COUNT(o) FROM CrmOpportunity o " +
           "WHERE o.organisation.id = :orgId AND o.stage IS NOT NULL " +
           "GROUP BY o.stage.id")
    List<Object[]> countByStage(@Param("orgId") Long orgId);
}
