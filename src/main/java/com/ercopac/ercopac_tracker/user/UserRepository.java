package com.ercopac.ercopac_tracker.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByOrganisation_Id(Long organisationId);

    long countByRole(Role role);

    Optional<AppUser> findByIdAndOrganisation_Id(Long id, Long organisationId);

    boolean existsByOrganisation_IdAndEmployeeCode(Long organisationId, String employeeCode);

    Optional<AppUser> findByOrganisation_IdAndEmployeeCode(Long organisationId, String employeeCode);

    List<AppUser> findByOrganisation_IdAndRoleOrderByFullNameAsc(Long organisationId, Role role);

    List<AppUser> findByOrganisation_IdAndDepartmentCodeOrderByFullNameAsc(Long organisationId, String departmentCode);

    List<AppUser> findByOrganisation_IdOrderByFullNameAsc(Long organisationId);

    Optional<AppUser> findFirstByOrganisationIdAndRole(Long organisationId, Role role);

    @Query("""
        select u
        from AppUser u
        where (:organisationId is null or u.organisation.id = :organisationId)
          and (
                :searchPattern is null or
                lower(coalesce(u.fullName, '')) like :searchPattern or
                lower(coalesce(u.email, '')) like :searchPattern or
                lower(coalesce(u.employeeCode, '')) like :searchPattern or
                lower(coalesce(u.departmentCode, '')) like :searchPattern or
                lower(coalesce(u.jobTitle, '')) like :searchPattern or
                lower(coalesce(u.resourceType, '')) like :searchPattern
              )
          and (:departmentCode is null or u.departmentCode = :departmentCode)
          and (:role is null or u.role = :role)
          and (:active is null or u.active = :active)
          and (:internalUser is null or u.internalUser = :internalUser)
        order by u.fullName asc
    """)
    Page<AppUser> searchResources(
            @Param("organisationId") Long organisationId,
            @Param("searchPattern") String searchPattern,
            @Param("departmentCode") String departmentCode,
            @Param("role") Role role,
            @Param("active") Boolean active,
            @Param("internalUser") Boolean internalUser,
            Pageable pageable
    );

    @Query("""
        select u
        from AppUser u
        where u.organisation.id = :organisationId
          and u.active = true
          and (:departmentCode is null or u.departmentCode = :departmentCode)
        order by u.fullName asc
    """)
    List<AppUser> findActiveResourcesByOrganisationId(
            @Param("organisationId") Long organisationId,
            @Param("departmentCode") String departmentCode
    );

    @Query("""
        select distinct u.departmentCode
        from AppUser u
        where u.organisation.id = :organisationId
          and u.departmentCode is not null
          and trim(u.departmentCode) <> ''
        order by u.departmentCode asc
    """)
    List<String> findDistinctDepartmentCodesByOrganisationId(@Param("organisationId") Long organisationId);

    @Query("""
        select distinct u.resourceType
        from AppUser u
        where u.organisation.id = :organisationId
          and u.resourceType is not null
          and trim(u.resourceType) <> ''
        order by u.resourceType asc
    """)
    List<String> findDistinctResourceTypesByOrganisationId(@Param("organisationId") Long organisationId);

    @Query("""
        select distinct u.seniority
        from AppUser u
        where u.organisation.id = :organisationId
          and u.seniority is not null
          and trim(u.seniority) <> ''
        order by u.seniority asc
    """)
    List<String> findDistinctSenioritiesByOrganisationId(@Param("organisationId") Long organisationId);
}