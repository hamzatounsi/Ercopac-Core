package com.ercopac.ercopac_tracker.crm.repository;
 
import com.ercopac.ercopac_tracker.crm.domain.CrmPipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface CrmPipelineStageRepository extends JpaRepository<CrmPipelineStage, Long> {
 
    List<CrmPipelineStage> findByOrganisation_IdOrderByDisplayOrderAsc(Long orgId);
 
    long countByOrganisation_Id(Long orgId);
 
    boolean existsByOrganisation_IdAndName(Long orgId, String name);
}
 