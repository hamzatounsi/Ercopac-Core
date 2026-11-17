package com.ercopac.ercopac_tracker.admin.repository;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByOrganisation_IdOrderByNameAsc(Long organisationId);

    Optional<Customer> findByIdAndOrganisation_Id(Long id, Long organisationId);

    Optional<Customer> findByOrganisation_IdAndCustomerCodeIgnoreCase(Long organisationId, String customerCode);

    boolean existsByOrganisation_IdAndCustomerCodeIgnoreCase(Long organisationId, String customerCode);

    Optional<Customer> findFirstByOrganisation_IdAndNameIgnoreCase(Long organisationId, String name);

    /** Case-insensitive name uniqueness inside one organisation (names are stored normalised). */
    boolean existsByOrganisation_IdAndNameIgnoreCase(Long organisationId, String name);

    boolean existsByOrganisation_IdAndNameIgnoreCaseAndIdNot(Long organisationId, String name, Long id);

    boolean existsByOrganisation_IdAndCustomerCodeIgnoreCaseAndIdNot(Long organisationId, String customerCode, Long id);

    /**
     * Customer Management list: organisation-scoped, optional active filter and a
     * case-insensitive search over name, code, town, country, contact and email.
     * {@code pattern} is already lower-cased and wrapped in '%'.
     */
    @Query("""
            select c from Customer c
            where c.organisation.id = :organisationId
              and (:filterActive = false or c.active = :active)
              and (lower(c.name) like :pattern
                   or lower(c.customerCode) like :pattern
                   or lower(coalesce(c.town, '')) like :pattern
                   or lower(coalesce(c.country, '')) like :pattern
                   or lower(coalesce(c.contactPerson, '')) like :pattern
                   or lower(coalesce(c.email, '')) like :pattern)
            """)
    Page<Customer> search(
            @Param("organisationId") Long organisationId,
            @Param("filterActive") boolean filterActive,
            @Param("active") boolean active,
            @Param("pattern") String pattern,
            Pageable pageable
    );

    /** Dropdown options: active customers of one organisation matching a name or code fragment. */
    @Query("""
            select c from Customer c
            where c.organisation.id = :organisationId
              and c.active = true
              and (lower(c.name) like :pattern or lower(c.customerCode) like :pattern)
            order by c.name asc
            """)
    List<Customer> findActiveOptions(
            @Param("organisationId") Long organisationId,
            @Param("pattern") String pattern,
            Pageable pageable
    );
}
