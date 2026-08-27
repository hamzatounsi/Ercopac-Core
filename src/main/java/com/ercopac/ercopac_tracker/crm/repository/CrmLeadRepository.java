package com.ercopac.ercopac_tracker.crm.repository;
 
import com.ercopac.ercopac_tracker.crm.domain.CrmLead;
import com.ercopac.ercopac_tracker.crm.domain.CrmLead.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
 
public interface CrmLeadRepository extends JpaRepository<CrmLead, Long> {
 
    List<CrmLead> findByOrganisation_IdAndActiveTrueOrderByCreatedAtDesc(Long orgId);
 
    List<CrmLead> findByOrganisation_IdAndStatusAndActiveTrueOrderByCreatedAtDesc(
            Long orgId, Status status);
 
    List<CrmLead> findByOrganisation_IdAndConvertedFalseAndActiveTrueOrderByCreatedAtDesc(
            Long orgId);

    Optional<CrmLead> findByIdAndOrganisation_Id(Long id, Long orgId);

    List<CrmLead> findByOrganisation_IdAndAccount_IdAndActiveTrueOrderByFullNameAsc(Long orgId, Long accountId);

    long countByOrganisation_IdAndAccount_IdAndActiveTrue(Long orgId, Long accountId);
 
    // Search by name or company
    @Query("SELECT l FROM CrmLead l WHERE l.organisation.id = :orgId " +
           "AND l.active = true " +
           "AND (LOWER(l.fullName) LIKE LOWER(CONCAT('%',:term,'%')) " +
           "OR LOWER(COALESCE(l.account.name,l.company,'')) LIKE LOWER(CONCAT('%',:term,'%')))" )
    List<CrmLead> searchByOrgAndTerm(@Param("orgId") Long orgId, @Param("term") String term);
 
    long countByOrganisation_IdAndActiveTrue(Long orgId);
 
    // Lead-source aggregation for the CRM dashboard and reports.
    @Query("SELECT l.source, COUNT(l) FROM CrmLead l " +
           "WHERE l.organisation.id = :orgId AND l.active = true " +
           "GROUP BY l.source")
    List<Object[]> countBySource(@Param("orgId") Long orgId);
}
