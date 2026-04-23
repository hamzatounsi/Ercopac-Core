package com.ercopac.ercopac_tracker.projectum.risks.repository;

import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiskItemRepository extends JpaRepository<RiskItem, Long> {

    List<RiskItem> findAllByProjectIdOrderByIdAsc(Long projectId);

    List<RiskItem> findAllByProjectIdAndOrganisationIdOrderByIdAsc(Long projectId, Long organisationId);

    Optional<RiskItem> findByIdAndProjectIdAndOrganisationId(Long id, Long projectId, Long organisationId);

    boolean existsByProjectIdAndWbsCodeAndDescription(Long projectId, String wbsCode, String description);
}