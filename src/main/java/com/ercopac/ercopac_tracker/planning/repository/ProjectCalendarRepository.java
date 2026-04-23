package com.ercopac.ercopac_tracker.planning.repository;

import com.ercopac.ercopac_tracker.planning.domain.ProjectCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectCalendarRepository extends JpaRepository<ProjectCalendar, Long> {

    List<ProjectCalendar> findByProjectIdAndOrganisationIdOrderByIdDesc(
            Long projectId,
            Long organisationId
    );

    Optional<ProjectCalendar> findByIdAndOrganisationId(
            Long id,
            Long organisationId
    );

    Optional<ProjectCalendar> findByProjectIdAndOrganisationIdAndIsDefaultTrue(
            Long projectId,
            Long organisationId
    );
}