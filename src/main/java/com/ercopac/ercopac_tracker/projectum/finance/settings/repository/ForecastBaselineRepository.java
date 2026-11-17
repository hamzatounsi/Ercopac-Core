package com.ercopac.ercopac_tracker.projectum.finance.settings.repository;

import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.ForecastBaseline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ForecastBaselineRepository extends JpaRepository<ForecastBaseline, Long> {

    List<ForecastBaseline> findAllByOrganisationIdAndProjectIdOrderByWbsCodeAsc(Long organisationId, Long projectId);

    void deleteAllByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}