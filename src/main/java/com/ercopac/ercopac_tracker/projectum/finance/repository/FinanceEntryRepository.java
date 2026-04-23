package com.ercopac.ercopac_tracker.projectum.finance.repository;

import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinanceEntryRepository extends JpaRepository<FinanceEntry, Long> {

    List<FinanceEntry> findAllByProjectIdOrderByWbsCodeAsc(Long projectId);

    List<FinanceEntry> findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(Long projectId, Long organisationId);

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