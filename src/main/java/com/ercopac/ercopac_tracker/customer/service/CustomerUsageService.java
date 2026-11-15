package com.ercopac.ercopac_tracker.customer.service;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.common.NameNormalizer;
import com.ercopac.ercopac_tracker.customer.dto.CustomerReferences;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Counts every place a customer is referenced. Kept in one class so a new reference
 * (another entity pointing at customers) only needs one more query here.
 */
@Service
@Transactional(readOnly = true)
public class CustomerUsageService {

    private final EntityManager entityManager;

    public CustomerUsageService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public CustomerReferences referencesOf(Customer customer) {
        Long customerId = customer.getId();
        Long organisationId = customer.getOrganisation().getId();

        // Projects saved before the customer_id link only carry the customer name as text.
        long projects = count("""
                select count(p) from Project p
                where p.organisation.id = :organisationId
                  and (p.customerEntity.id = :customerId
                       or (p.customerEntity is null and upper(trim(p.customer)) = :nameKey))
                """, customerId, organisationId, NameNormalizer.matchKey(customer.getName()));

        long tickets = count("select count(t) from Ticket t where t.customer.id = :customerId", customerId);

        long ticketingRecords =
                count("select count(x) from TicketCustomerSite x where x.customer.id = :customerId", customerId)
                + count("select count(x) from TicketCustomerContact x where x.customer.id = :customerId", customerId)
                + count("select count(x) from TicketCustomerEquipment x where x.customer.id = :customerId", customerId)
                + count("select count(x) from TicketCustomerProject x where x.customer.id = :customerId", customerId);

        return new CustomerReferences(projects, tickets, ticketingRecords);
    }

    private long count(String jpql, Long customerId) {
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("customerId", customerId)
                .getSingleResult();
    }

    private long count(String jpql, Long customerId, Long organisationId, String nameKey) {
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("customerId", customerId)
                .setParameter("organisationId", organisationId)
                .setParameter("nameKey", nameKey == null ? "" : nameKey)
                .getSingleResult();
    }
}
