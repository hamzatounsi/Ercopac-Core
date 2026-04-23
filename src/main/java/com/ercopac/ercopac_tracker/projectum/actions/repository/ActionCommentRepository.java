package com.ercopac.ercopac_tracker.projectum.actions.repository;

import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionCommentRepository extends JpaRepository<ActionComment, Long> {
}