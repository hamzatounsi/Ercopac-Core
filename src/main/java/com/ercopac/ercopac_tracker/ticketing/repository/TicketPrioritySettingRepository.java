package com.ercopac.ercopac_tracker.ticketing.repository;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketPrioritySetting; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketPrioritySettingRepository extends JpaRepository<TicketPrioritySetting,Long> { List<TicketPrioritySetting> findByOrganisation_Id(Long org); }
