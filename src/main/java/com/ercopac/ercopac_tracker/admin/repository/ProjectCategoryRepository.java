package com.ercopac.ercopac_tracker.admin.repository;

import com.ercopac.ercopac_tracker.admin.domain.ProjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, Long> {

    List<ProjectCategory> findByOrganisation_IdOrderByNameAsc(Long organisationId);

    Optional<ProjectCategory> findByIdAndOrganisation_Id(Long id, Long organisationId);

    boolean existsByOrganisation_IdAndCodeIgnoreCase(Long organisationId, String code);
}