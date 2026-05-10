package com.ercopac.ercopac_tracker.crm.repository;
 
import com.ercopac.ercopac_tracker.crm.domain.CrmLead;
import com.ercopac.ercopac_tracker.crm.domain.CrmLead.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
 
public interface CrmLeadRepository extends JpaRepository<CrmLead, Long> {
 
    List<CrmLead> findByOrganisation_IdAndActiveTrueOrderByCreatedAtDesc(Long orgId);
 
    List<CrmLead> findByOrganisation_IdAndStatusAndActiveTrueOrderByCreatedAtDesc(
            Long orgId, Status status);
 
    List<CrmLead> findByOrganisation_IdAndConvertedFalseAndActiveTrueOrderByCreatedAtDesc(
            Long orgId);
 
    // Search by name or company
    @Query("SELECT l FROM CrmLead l WHERE l.organisation.id = :orgId " +
           "AND l.active = true " +
           "AND (LOWER(l.fullName) LIKE LOWER(CONCAT('%',:term,'%')) " +
           "OR LOWER(l.company) LIKE LOWER(CONCAT('%',:term,'%')))")
    List<CrmLead> searchByOrgAndTerm(@Param("orgId") Long orgId, @Param("term") String term);
 
    long countByOrganisation_IdAndActiveTrue(Long orgId);
 
    // Count by source for analytics
    @Query("SELECT l.source, COUNT(l) FROM CrmLead l " +
           "WHERE l.organisation.id = :orgId AND l.active = true " +
           "GROUP BY l.source")
    List<Object[]> countBySource(@Param("orgId") Long orgId);
}