package com.ercopac.ercopac_tracker.projectum.finance.settings.repository;

import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsTemplateRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceWbsTemplateRowRepository extends JpaRepository<FinanceWbsTemplateRow, Long> {

    // ✅ Trouver les lignes de template pour un projet spécifique, triées par ordre
    List<FinanceWbsTemplateRow> findAllByProjectIdOrderBySortOrderAscIdAsc(Long projectId);

    // ✅ Trouver les lignes de template globales (organisation, sans projet), triées par ordre
    List<FinanceWbsTemplateRow> findAllByOrganisationIdAndProjectIsNullOrderBySortOrderAscIdAsc(Long organisationId);

    // ✅ Supprimer toutes les lignes pour un projet
    void deleteAllByProjectId(Long projectId);

    // ✅ Supprimer toutes les lignes globales pour une organisation
    void deleteAllByOrganisationId(Long organisationId);
}