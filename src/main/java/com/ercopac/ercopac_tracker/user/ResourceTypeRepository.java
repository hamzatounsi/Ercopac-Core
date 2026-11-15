package com.ercopac.ercopac_tracker.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {

    Optional<ResourceType> findByCodeAndOrganisation_Id(String code, Long organisationId);

    

    Optional<ResourceType> findByIdAndOrganisation_Id(Long id, Long organisationId);

    List<ResourceType> findByOrganisation_IdOrderByCodeAsc(Long organisationId);

    List<ResourceType> findByOrganisation_IdAndActiveTrueAndAssignableTrueOrderByCodeAsc(Long organisationId);

    boolean existsByCodeAndOrganisation_Id(String code, Long organisationId);
    List<ResourceType> findByOrganisation_IdAndActiveTrue(Long organisationId);
    
    List<ResourceType> findByOrganisationIdAndActiveTrueOrderByCodeAsc(Long organisationId);

    /** Active resource types linked to a department, matched by department code ignoring case. */
    @Query("""
        select rt from ResourceType rt
        where rt.organisation.id = :organisationId
          and rt.active = true
          and upper(rt.department.code) = upper(:departmentCode)
        order by rt.code asc
    """)
    List<ResourceType> findActiveByOrganisationAndDepartmentCode(@Param("organisationId") Long organisationId,
                                                                 @Param("departmentCode") String departmentCode);
    Optional<ResourceType> findByCodeAndOrganisationId(String code, Long organisationId);
}