package com.ercopac.ercopac_tracker.projectum.actions.repository;

import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionAssignee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionAssigneeRepository extends JpaRepository<ActionAssignee, Long> {
}