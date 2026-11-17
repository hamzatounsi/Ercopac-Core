package com.ercopac.ercopac_tracker.integration.repository;

import com.ercopac.ercopac_tracker.integration.entity.ErpWbsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ErpWbsItemRepository extends JpaRepository<ErpWbsItem, Long> {
    Optional<ErpWbsItem> findByErpItemId(Long erpItemId);
    List<ErpWbsItem> findByProjectId(Long projectId);
}