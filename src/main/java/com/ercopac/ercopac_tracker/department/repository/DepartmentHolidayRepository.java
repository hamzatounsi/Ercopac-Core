package com.ercopac.ercopac_tracker.department.repository;

import com.ercopac.ercopac_tracker.department.domain.DepartmentHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DepartmentHolidayRepository extends JpaRepository<DepartmentHoliday, Long> {

    List<DepartmentHoliday> findByOrganisationIdAndMember_IdIn(Long organisationId, Collection<Long> memberIds);

    Optional<DepartmentHoliday> findByOrganisationIdAndMember_IdAndFromDateAndToDate(
            Long organisationId,
            Long memberId,
            LocalDate fromDate,
            LocalDate toDate
    );

    boolean existsByOrganisationIdAndMember_IdAndFromDateAndToDate(
            Long organisationId,
            Long memberId,
            LocalDate fromDate,
            LocalDate toDate
    );

    Optional<DepartmentHoliday> findByIdAndOrganisationId(Long id, Long organisationId);
}