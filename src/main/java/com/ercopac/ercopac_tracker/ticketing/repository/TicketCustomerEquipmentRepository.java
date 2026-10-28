package com.ercopac.ercopac_tracker.ticketing.repository;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketCustomerEquipment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketCustomerEquipmentRepository extends JpaRepository<TicketCustomerEquipment,Long> { List<TicketCustomerEquipment> findByOrganisation_IdAndCustomer_IdOrderByNameAsc(Long org,Long customerId); Optional<TicketCustomerEquipment> findByIdAndOrganisation_Id(Long id,Long org); long countByCustomer_Id(Long customerId); long countBySite_Id(Long siteId); }
