package com.ercopac.ercopac_tracker.projectum.actions.repository;

import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {

    List<ActionItem> findAllByProjectIdOrderByIdAsc(Long projectId);

    List<ActionItem> findAllByProjectIdAndOrganisationIdOrderByIdAsc(Long projectId, Long organisationId);

    Optional<ActionItem> findByIdAndProjectIdAndOrganisationId(Long id, Long projectId, Long organisationId);

    boolean existsByProjectIdAndTitle(Long projectId, String title);

    List<ActionItem> findAllByOwner_IdAndOrganisationIdOrderByDueDateAscIdAsc(Long ownerId, Long organisationId);

    List<ActionItem> findAllByDepartmentCodeAndOrganisationIdOrderByDueDateAscIdAsc(String departmentCode, Long organisationId);

    List<ActionItem> findAllByDepartment_IdAndOrganisationIdOrderByDueDateAscIdAsc(Long departmentId, Long organisationId);

    List<ActionItem> findAllByRisk_IdAndOrganisationIdOrderByIdAsc(Long riskId, Long organisationId);

    List<ActionItem> findAllByChangeRequest_IdAndOrganisationIdOrderByIdAsc(Long changeRequestId, Long organisationId);

    List<ActionItem> findAllByProjectTask_IdAndOrganisationIdOrderByIdAsc(Long taskId, Long organisationId);

    // ✅ NOUVEAU : Récupérer les actions actives de l'utilisateur connecté
    @Query("SELECT DISTINCT a FROM ActionItem a LEFT JOIN a.assignees ass " +
           "WHERE (a.owner.id = :userId OR ass.assigneeUser.id = :userId) " +
           "AND a.organisation.id = :orgId AND a.status != 'done' " +
           "ORDER BY a.dueDate ASC, a.id ASC")
    List<ActionItem> findMyActiveActions(@Param("userId") Long userId, @Param("orgId") Long orgId);
}