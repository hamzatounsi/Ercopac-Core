package com.ercopac.ercopac_tracker.projectum.change_requests.repository;

import com.ercopac.ercopac_tracker.projectum.change_requests.domain.ChangeRequestAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeRequestAttachmentRepository extends JpaRepository<ChangeRequestAttachment, Long> {
}