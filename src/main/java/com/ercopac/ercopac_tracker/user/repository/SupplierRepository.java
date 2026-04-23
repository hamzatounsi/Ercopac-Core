package com.ercopac.ercopac_tracker.user.repository;

import com.ercopac.ercopac_tracker.user.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByOrganisation_IdAndActiveTrueOrderByNameAsc(Long organisationId);

    Optional<Supplier> findByIdAndOrganisation_Id(Long id, Long organisationId);
}