package com.ercopac.ercopac_tracker.organisation.repository;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    Optional<Organisation> findByCode(String code);
    
}