package com.ercopac.ercopac_tracker.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {

    Optional<ResourceType> findByCodeAndOrganisationId(String code, Long organisationId);

    List<ResourceType> findByOrganisationIdOrderByCodeAsc(Long organisationId);
}