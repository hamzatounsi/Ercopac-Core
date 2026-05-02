package com.ercopac.ercopac_tracker.admin.repository;

import com.ercopac.ercopac_tracker.admin.domain.AdminLicenceAssignment;
import com.ercopac.ercopac_tracker.admin.domain.AdminLicenceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminLicenceAssignmentRepository extends JpaRepository<AdminLicenceAssignment, Long> {

    List<AdminLicenceAssignment> findByOrganisation_IdOrderByUser_FullNameAsc(Long organisationId);

    Optional<AdminLicenceAssignment> findByOrganisation_IdAndUser_Id(Long organisationId, Long userId);

    long countByOrganisation_IdAndLicenceType(Long organisationId, AdminLicenceType licenceType);

    void deleteByOrganisation_IdAndUser_Id(Long organisationId, Long userId);
}