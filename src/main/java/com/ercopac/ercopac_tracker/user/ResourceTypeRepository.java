package com.ercopac.ercopac_tracker.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {

    Optional<ResourceType> findByCodeAndOrganisation_Id(String code, Long organisationId);

    Optional<ResourceType> findByCodeAndOrganisationId(String code, Long organisationId);

    Optional<ResourceType> findByIdAndOrganisation_Id(Long id, Long organisationId);

    List<ResourceType> findByOrganisation_IdOrderByCodeAsc(Long organisationId);

    List<ResourceType> findByOrganisation_IdAndActiveTrueAndAssignableTrueOrderByCodeAsc(Long organisationId);

    boolean existsByCodeAndOrganisation_Id(String code, Long organisationId);
}