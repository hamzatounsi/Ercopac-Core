package com.ercopac.ercopac_tracker.ticketing.repository;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketCategorySetting; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketCategorySettingRepository extends JpaRepository<TicketCategorySetting,Long> { List<TicketCategorySetting> findByOrganisation_IdOrderBySortOrderAscIdAsc(Long org); Optional<TicketCategorySetting> findByIdAndOrganisation_Id(Long id,Long org); Optional<TicketCategorySetting> findByOrganisation_IdAndCodeIgnoreCase(Long org,String code); }
