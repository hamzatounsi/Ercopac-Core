package com.ercopac.ercopac_tracker.projectum.actions.repository;

import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionAttachmentRepository extends JpaRepository<ActionAttachment, Long> {
}