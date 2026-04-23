package com.ercopac.ercopac_tracker.projectum.change_requests.repository;

import com.ercopac.ercopac_tracker.projectum.change_requests.domain.ChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {

    List<ChangeRequest> findAllByProjectIdOrderByIdAsc(Long projectId);

    List<ChangeRequest> findAllByProjectIdAndOrganisationIdOrderByIdAsc(Long projectId, Long organisationId);

    Optional<ChangeRequest> findByIdAndProjectIdAndOrganisationId(Long id, Long projectId, Long organisationId);
}