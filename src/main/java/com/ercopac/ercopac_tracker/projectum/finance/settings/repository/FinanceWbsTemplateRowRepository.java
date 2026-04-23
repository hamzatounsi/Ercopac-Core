package com.ercopac.ercopac_tracker.projectum.finance.settings.repository;

import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsTemplateRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceWbsTemplateRowRepository extends JpaRepository<FinanceWbsTemplateRow, Long> {
    List<FinanceWbsTemplateRow> findAllByOrganisationIdOrderBySortOrderAscIdAsc(Long organisationId);
    void deleteAllByOrganisationId(Long organisationId);
}