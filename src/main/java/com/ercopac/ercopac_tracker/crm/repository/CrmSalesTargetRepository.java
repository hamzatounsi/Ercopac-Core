
package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmSalesTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CrmSalesTargetRepository extends JpaRepository<CrmSalesTarget, Long> {

    List<CrmSalesTarget> findByOrganisation_IdAndTargetYear(
            Long organisationId,
            Integer targetYear
    );

    Optional<CrmSalesTarget> findByOrganisation_IdAndUser_IdAndTargetYear(
            Long organisationId,
            Long userId,
            Integer targetYear
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM CrmSalesTarget t
        WHERE t.organisation.id = :orgId
          AND t.targetYear = :year
    """)
    BigDecimal sumAmountByOrganisation_IdAndTargetYear(
            @Param("orgId") Long orgId,
            @Param("year") int year
    );
}
