package com.ercopac.ercopac_tracker.department.repository;

import com.ercopac.ercopac_tracker.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCodeAndOrganisation_Id(String code, Long organisationId);

    Optional<Department> findByIdAndOrganisation_Id(Long id, Long organisationId);

    List<Department> findByOrganisation_IdOrderByCodeAsc(Long organisationId);

    boolean existsByCodeAndOrganisation_Id(String code, Long organisationId);
}