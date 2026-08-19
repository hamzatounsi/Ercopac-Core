package com.ercopac.ercopac_tracker.projectum.finance.repository;

import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinanceEntryRepository extends JpaRepository<FinanceEntry, Long> {
    List<FinanceEntry> findAllByOrganisationId(Long organisationId);

    // --- Anciennes méthodes (gardées pour compatibilité) ---
    List<FinanceEntry> findAllByProjectIdOrderByWbsCodeAsc(Long projectId);
    List<FinanceEntry> findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(Long projectId, Long organisationId);

    // ✅ NOUVEAUX : Trier par displayOrder (ordre du template), puis par WBS Code
    
    // Pour les utilisateurs Platform (sans filtre organisation)
    List<FinanceEntry> findAllByProjectIdOrderByDisplayOrderAscWbsCodeAsc(Long projectId);

    // Pour les utilisateurs normaux (avec filtre organisation)
    List<FinanceEntry> findAllByProjectIdAndOrganisationIdOrderByDisplayOrderAscWbsCodeAsc(Long projectId, Long organisationId);

    // --- Autres méthodes existantes ---
    Optional<FinanceEntry> findByIdAndProjectIdAndOrganisationId(Long id, Long projectId, Long organisationId);
    void deleteByIdAndProjectIdAndOrganisationId(Long id, Long projectId, Long organisationId);
    boolean existsByProjectIdAndWbsCode(Long projectId, String wbsCode);
    
    List<FinanceEntry> findAllByProjectIdAndOrganisationIdAndWbsCodeStartingWithOrderByWbsCodeAsc(
        Long projectId, Long organisationId, String wbsPrefix);

    List<FinanceEntry> findAllByProjectIdAndWbsCodeStartingWithOrderByWbsCodeAsc(
            Long projectId, String wbsPrefix);

    Optional<FinanceEntry> findByIdAndProjectId(Long id, Long projectId);
    void deleteAllByProjectIdAndOrganisationId(Long projectId, Long organisationId);
}
