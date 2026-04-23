package com.ercopac.ercopac_tracker.projectum.forecast.repository;

import com.ercopac.ercopac_tracker.projectum.forecast.domain.ForecastEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ForecastEntryRepository extends JpaRepository<ForecastEntry, Long> {

    List<ForecastEntry> findAllByProjectIdOrderByWbsCodeAscPeriodKeyAsc(Long projectId);

    List<ForecastEntry> findAllByProjectIdAndOrganisationIdOrderByWbsCodeAscPeriodKeyAsc(Long projectId, Long organisationId);

    Optional<ForecastEntry> findByProjectIdAndOrganisationIdAndWbsCodeAndPeriodKey(
            Long projectId, Long organisationId, String wbsCode, String periodKey
    );

    Optional<ForecastEntry> findByProjectIdAndWbsCodeAndPeriodKey(
            Long projectId, String wbsCode, String periodKey
    );

    boolean existsByProjectIdAndWbsCodeAndPeriodKey(Long projectId, String wbsCode, String periodKey);

}