package com.ercopac.ercopac_tracker.ticketing.repository;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatusSetting; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketStatusSettingRepository extends JpaRepository<TicketStatusSetting,Long> { List<TicketStatusSetting> findByOrganisation_Id(Long org); }
