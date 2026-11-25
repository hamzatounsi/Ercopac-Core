package com.ercopac.ercopac_tracker.ticketing.repository;
import com.ercopac.ercopac_tracker.ticketing.domain.*; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.Instant; import java.util.*;
public interface TicketRepository extends JpaRepository<Ticket,Long> {
 @Query("select t from Ticket t where (:platform = true or t.organisation.id = :organisationId) and (:clientOnly = false or (:scopeCustomerId is not null and t.customer.id = :scopeCustomerId) or (:scopeCustomerId is null and (t.clientUser.id = :userId or t.createdByUser.id = :userId))) and (:search is null or lower(t.ticketNumber) like :search or lower(t.subject) like :search) and (:statusId is null or t.status.id = :statusId) and (:statusCode is null or t.status.code = :statusCode) and (:priority is null or t.priority = :priority) and (:category is null or t.category = :category) and (:assigneeId is null or t.assignedSalesManager.id = :assigneeId) and (:creatorId is null or t.createdByUser.id = :creatorId) and (:customerId is null or t.customer.id = :customerId) and t.createdAt >= :fromDate and t.createdAt <= :toDate")
 Page<Ticket> search(@Param("platform") boolean platform,@Param("organisationId") Long organisationId,@Param("clientOnly") boolean clientOnly,@Param("userId") Long userId,@Param("scopeCustomerId") Long scopeCustomerId,@Param("search") String search,@Param("statusId") Long statusId,@Param("statusCode") String statusCode,@Param("priority") TicketPriority priority,@Param("category") String category,@Param("assigneeId") Long assigneeId,@Param("creatorId") Long creatorId,@Param("customerId") Long customerId,@Param("fromDate") Instant fromDate,@Param("toDate") Instant toDate,Pageable pageable);
 Optional<Ticket> findFirstByTicketNumberStartingWithOrderByTicketNumberDesc(String prefix);
 Optional<Ticket> findByTicketNumber(String ticketNumber);
 // Visible tickets per status code: rows of [code, closed, count]
 @Query("select s.code, s.closed, count(t) from Ticket t join t.status s where (:platform = true or t.organisation.id = :organisationId) and (:clientOnly = false or (:scopeCustomerId is not null and t.customer.id = :scopeCustomerId) or (:scopeCustomerId is null and (t.clientUser.id = :userId or t.createdByUser.id = :userId))) group by s.code, s.closed") List<Object[]> countVisibleByStatus(boolean platform,Long organisationId,boolean clientOnly,Long userId,Long scopeCustomerId);
 @Query("select count(t) from Ticket t where (:platform = true or t.organisation.id = :organisationId) and (:clientOnly = false or (:scopeCustomerId is not null and t.customer.id = :scopeCustomerId) or (:scopeCustomerId is null and (t.clientUser.id = :userId or t.createdByUser.id = :userId))) and t.createdAt >= :today") long countVisibleCreatedSince(boolean platform,Long organisationId,boolean clientOnly,Long userId,Long scopeCustomerId,Instant today);
 // clientOnly: CLIENT portal scope. scopeCustomerId set (CUSTOMER account): every ticket of that customer; null: the client's own tickets
 // Tickets of an organisation created in a period (optionally of one customer), for the performance dashboard
 @Query("select t from Ticket t where t.organisation.id = :organisationId and t.createdAt >= :fromDate and t.createdAt < :toDate and (:customerId is null or t.customer.id = :customerId)")
 List<Ticket> findForAnalytics(@Param("organisationId") Long organisationId,@Param("fromDate") Instant fromDate,@Param("toDate") Instant toDate,@Param("customerId") Long customerId);
 // Unfinished tickets of an organisation, whatever their age (open work, overdue and agent load)
 @Query("select t from Ticket t left join t.status s where t.organisation.id = :organisationId and (s is null or s.closed = false)")
 List<Ticket> findActive(@Param("organisationId") Long organisationId);
 @Query("select count(t) from Ticket t left join t.status s where t.customer.id = :customerId and (s is null or s.closed = false)")
 long countOpenByCustomer(@Param("customerId") Long customerId);
 long countByProject_Id(Long projectId);
 boolean existsByEquipment_Id(Long equipmentId);
 // Ticket settings: categories and statuses in use
 long countByOrganisation_IdAndCategory(Long organisationId,String category);
 long countByStatus_Id(Long statusId);
 @Modifying @Query("update Ticket t set t.category = :to where t.organisation.id = :org and t.category = :from")
 int reassignCategory(@Param("org") Long organisationId,@Param("from") String from,@Param("to") String to);
 // Native so the bulk move never joins the status table; version is bumped so open editors see the change
 @Modifying @Query(value = "update tickets set status_id = :to, version = coalesce(version, 0) + 1 where organisation_id = :org and status_id = :from", nativeQuery = true)
 int reassignStatus(@Param("org") Long organisationId,@Param("from") Long fromStatusId,@Param("to") Long toStatusId);
 // Ticket deletion: children removed with the ticket
 @Modifying @Query("delete from TicketReadState r where r.ticket.id = :id") int deleteReadStates(@Param("id") Long ticketId);
 @Modifying @Query("delete from TicketActivity a where a.ticket.id = :id") int deleteActivities(@Param("id") Long ticketId);
 @Modifying @Query("delete from TicketAttachment a where a.ticket.id = :id") int deleteAttachments(@Param("id") Long ticketId);
 @Modifying @Query("delete from TicketMessage m where m.ticket.id = :id") int deleteMessages(@Param("id") Long ticketId);
}
