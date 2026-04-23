package com.ercopac.ercopac_tracker.projectum.change_requests.repository;

import com.ercopac.ercopac_tracker.projectum.change_requests.domain.ChangeRequestHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeRequestHistoryRepository extends JpaRepository<ChangeRequestHistoryEntry, Long> {
}