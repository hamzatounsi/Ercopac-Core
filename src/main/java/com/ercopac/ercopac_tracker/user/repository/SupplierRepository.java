package com.ercopac.ercopac_tracker.user.repository;

import com.ercopac.ercopac_tracker.user.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByOrganisation_IdAndActiveTrueOrderByNameAsc(Long organisationId);

    List<Supplier> findByOrganisation_IdOrderByNameAsc(Long organisationId);

    Optional<Supplier> findByIdAndOrganisation_Id(Long id, Long organisationId);

    @Query("""
            select count(s) from Supplier s
            where s.organisation.id = :organisationId
              and (lower(s.code) = lower(:code) or lower(s.shortCode) = lower(:code))
            """)
    long countByOrganisationAndCodeOrLegacyShortCode(
            @Param("organisationId") Long organisationId,
            @Param("code") String code
    );

    @Query("""
            select count(s) from Supplier s
            where s.organisation.id = :organisationId and s.id <> :id
              and (lower(s.code) = lower(:code) or lower(s.shortCode) = lower(:code))
            """)
    long countByOrganisationAndCodeOrLegacyShortCodeExcludingId(
            @Param("organisationId") Long organisationId,
            @Param("code") String code,
            @Param("id") Long id
    );
}
