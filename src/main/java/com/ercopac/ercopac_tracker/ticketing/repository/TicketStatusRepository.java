package com.ercopac.ercopac_tracker.ticketing.repository;

import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketStatusRepository extends JpaRepository<TicketStatus, Long> {
    List<TicketStatus> findByOrganisation_IdOrderByDisplayOrderAscIdAsc(Long organisationId);
    Optional<TicketStatus> findByIdAndOrganisation_Id(Long id, Long organisationId);
    Optional<TicketStatus> findByOrganisation_IdAndCodeIgnoreCase(Long organisationId, String code);
    Optional<TicketStatus> findFirstByOrganisation_IdAndIsDefaultTrue(Long organisationId);
    boolean existsByOrganisation_Id(Long organisationId);
    boolean existsByOrganisation_IdAndCodeIgnoreCase(Long organisationId, String code);
}
