package com.ercopac.ercopac_tracker.department.repository;

import com.ercopac.ercopac_tracker.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    long countByOrganisation_Id(Long organisationId);

    Optional<Department> findByCodeAndOrganisation_Id(String code, Long organisationId);

    Optional<Department> findByIdAndOrganisation_Id(Long id, Long organisationId);

    List<Department> findByOrganisation_IdOrderByCodeAsc(Long organisationId);

    boolean existsByCodeAndOrganisation_Id(String code, Long organisationId);
    boolean existsByOrganisation_IdAndCodeIgnoreCase(Long organisationId, String code);
    boolean existsByOrganisation_IdAndCodeIgnoreCaseAndIdNot(Long organisationId, String code, Long id);
    boolean existsByOrganisation_IdAndLabelIgnoreCase(Long organisationId, String label);
    boolean existsByOrganisation_IdAndLabelIgnoreCaseAndIdNot(Long organisationId, String label, Long id);
    List<Department> findByOrganisationIdOrderByCodeAsc(Long organisationId);
    Optional<Department> findByCodeAndOrganisationId(String code, Long organisationId);
}
