package com.ercopac.ercopac_tracker.ticketing.repository;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketCustomStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketCustomStatusRepository extends JpaRepository<TicketCustomStatus,Long> { List<TicketCustomStatus> findByOrganisation_IdOrderByLabelAsc(Long org); Optional<TicketCustomStatus> findByIdAndOrganisation_Id(Long id,Long org); boolean existsByOrganisation_IdAndCodeIgnoreCase(Long org,String code); }
