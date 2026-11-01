package com.ercopac.ercopac_tracker.ticketing.repository;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketCustomerProject; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketCustomerProjectRepository extends JpaRepository<TicketCustomerProject,Long> { List<TicketCustomerProject> findByOrganisation_IdAndCustomer_IdOrderByNameAsc(Long org,Long customerId); Optional<TicketCustomerProject> findByIdAndOrganisation_Id(Long id,Long org); long countByCustomer_Id(Long customerId); }
