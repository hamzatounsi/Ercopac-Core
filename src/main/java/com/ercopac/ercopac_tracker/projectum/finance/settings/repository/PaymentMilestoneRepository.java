package com.ercopac.ercopac_tracker.projectum.finance.settings.repository;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.PaymentMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface PaymentMilestoneRepository extends JpaRepository<PaymentMilestone, Long> {
    List<PaymentMilestone> findAllByOrganisationIdAndProjectIdOrderByMilestoneOrderAsc(Long organisationId, Long projectId);
    void deleteAllByProjectId(Long projectId);
}
