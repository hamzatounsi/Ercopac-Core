package com.ercopac.ercopac_tracker.planning.repository;

import com.ercopac.ercopac_tracker.planning.domain.ProjectTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, Long> {

    List<ProjectTemplate> findByProjectIdAndOrganisationIdOrderByCreatedAtDesc(
            Long projectId,
            Long organisationId
    );

    Optional<ProjectTemplate> findByIdAndProjectIdAndOrganisationId(
            Long id,
            Long projectId,
            Long organisationId
    );
}