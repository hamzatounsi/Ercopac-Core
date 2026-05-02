package com.ercopac.ercopac_tracker.admin.repository;

import com.ercopac.ercopac_tracker.admin.domain.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, Long> {

    List<ProjectType> findByOrganisation_IdOrderByNameAsc(Long organisationId);

    Optional<ProjectType> findByIdAndOrganisation_Id(Long id, Long organisationId);

    boolean existsByOrganisation_IdAndCodeIgnoreCase(Long organisationId, String code);
}