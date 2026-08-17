package com.ercopac.ercopac_tracker.projectum.finance.settings.repository;

import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsTemplateRow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinanceWbsTemplateRowRepository extends JpaRepository<FinanceWbsTemplateRow, Long> {
    
    List<FinanceWbsTemplateRow> findAllByOrganisationIdOrderBySortOrderAscIdAsc(Long organisationId);
    
    // ✅ NOUVEAU : Trouver les templates d'un projet spécifique
    List<FinanceWbsTemplateRow> findAllByProjectIdOrderBySortOrderAscIdAsc(Long projectId);
    
    // ✅ NOUVEAU : Trouver les templates globaux (project_id est NULL)
    List<FinanceWbsTemplateRow> findAllByOrganisationIdAndProjectIsNullOrderBySortOrderAscIdAsc(Long organisationId);
    
    void deleteAllByOrganisationId(Long organisationId);
    
    // ✅ NOUVEAU : Supprimer les templates d'un projet spécifique
    void deleteAllByProjectId(Long projectId);
}