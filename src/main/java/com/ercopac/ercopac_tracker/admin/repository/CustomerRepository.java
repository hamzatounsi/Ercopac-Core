package com.ercopac.ercopac_tracker.admin.repository;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByOrganisation_IdOrderByNameAsc(Long organisationId);

    Optional<Customer> findByIdAndOrganisation_Id(Long id, Long organisationId);

    Optional<Customer> findByOrganisation_IdAndCustomerCodeIgnoreCase(Long organisationId, String customerCode);

    boolean existsByOrganisation_IdAndCustomerCodeIgnoreCase(Long organisationId, String customerCode);
}