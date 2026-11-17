package com.ercopac.ercopac_tracker.integration.repository;

import com.ercopac.ercopac_tracker.integration.entity.ErpProject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ErpProjectRepository extends JpaRepository<ErpProject, Long> {
    Optional<ErpProject> findByErpProjectCode(String erpProjectCode);
}