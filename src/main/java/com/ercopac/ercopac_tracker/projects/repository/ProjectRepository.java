package com.ercopac.ercopac_tracker.projects.repository;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Project> findByIdAndOrganisationId(Long id, Long organisationId);

    Optional<Project> findByCodeAndOrganisationId(String code, Long organisationId);

    List<Project> findAllByOrganisationId(Long organisationId);

    long countByOrganisationId(Long organisationId);
    
}