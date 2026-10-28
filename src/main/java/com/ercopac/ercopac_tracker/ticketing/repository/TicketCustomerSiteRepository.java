package com.ercopac.ercopac_tracker.ticketing.repository;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketCustomerSite; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketCustomerSiteRepository extends JpaRepository<TicketCustomerSite,Long> { List<TicketCustomerSite> findByOrganisation_IdAndCustomer_IdOrderByNameAsc(Long org,Long customerId); Optional<TicketCustomerSite> findByIdAndOrganisation_Id(Long id,Long org); long countByCustomer_Id(Long customerId); }
