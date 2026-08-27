package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CrmAccountRepository extends JpaRepository<CrmAccount, Long> {
    List<CrmAccount> findByOrganisation_IdAndActiveTrueOrderByNameAsc(Long organisationId);
    Optional<CrmAccount> findByIdAndOrganisation_Id(Long id, Long organisationId);
    Optional<CrmAccount> findByOrganisation_IdAndNameIgnoreCase(Long organisationId, String name);
    @Query("select a from CrmAccount a where a.organisation.id=:orgId and a.active=true and " +
           "(lower(a.name) like lower(concat('%',:term,'%')) or lower(coalesce(a.industry,'')) like lower(concat('%',:term,'%')) " +
           "or lower(coalesce(a.country,'')) like lower(concat('%',:term,'%')))")
    List<CrmAccount> search(@Param("orgId") Long organisationId, @Param("term") String term);
}
