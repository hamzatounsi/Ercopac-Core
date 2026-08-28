package com.ercopac.ercopac_tracker.milestone;

import com.ercopac.ercopac_tracker.milestone.domain.MilestoneType;
import com.ercopac.ercopac_tracker.milestone.domain.ProjectMilestone;
import com.ercopac.ercopac_tracker.milestone.repository.MilestoneTypeRepository;
import com.ercopac.ercopac_tracker.milestone.repository.ProjectMilestoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * One-time, idempotent data fix: some ProjectMilestone rows were created
 * without a milestoneTypeId (id 20, 24, 76, 72 as of 2026-08-28), which makes
 * them render as an untyped grey "-" badge in the timeline instead of a
 * colored, lettered badge.
 *
 * This runs on every application startup, but only touches rows that still
 * have milestoneTypeId == null, so it is safe to leave in place / redeploy
 * repeatedly. Once the affected rows are fixed, it becomes a no-op.
 *
 * Default fallback type code is "LAU" (Launch), matching the type already
 * used on other milestones (e.g. SAP-2026). If a MilestoneType with that
 * code does not exist yet, one is created automatically.
 */
@Component
public class MilestoneDataFixRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MilestoneDataFixRunner.class);

    // The 4 orphaned milestone rows identified via GET /api/milestones/range
    private static final List<Long> ORPHANED_MILESTONE_IDS = List.of(20L, 24L, 76L, 72L);

    // Fallback milestone type code/label/color/letter used if nothing else applies
    private static final String DEFAULT_TYPE_CODE = "LAU";
    private static final String DEFAULT_TYPE_LABEL = "Launch";
    private static final String DEFAULT_TYPE_COLOR = "#8B5E3C";
    private static final String DEFAULT_TYPE_LETTER = "LAU";

    private final ProjectMilestoneRepository milestoneRepository;
    private final MilestoneTypeRepository milestoneTypeRepository;

    public MilestoneDataFixRunner(ProjectMilestoneRepository milestoneRepository,
                                   MilestoneTypeRepository milestoneTypeRepository) {
        this.milestoneRepository = milestoneRepository;
        this.milestoneTypeRepository = milestoneTypeRepository;
    }

    @Override
    public void run(String... args) {
        try {
            List<ProjectMilestone> candidates = milestoneRepository.findAllById(ORPHANED_MILESTONE_IDS);

            List<ProjectMilestone> toFix = candidates.stream()
                    .filter(m -> m.getMilestoneTypeId() == null)
                    .toList();

            if (toFix.isEmpty()) {
                log.info("[MilestoneDataFixRunner] Nothing to fix — all target milestones already have a type.");
                return;
            }

            Long fallbackTypeId = resolveOrCreateFallbackType();

            for (ProjectMilestone m : toFix) {
                m.setMilestoneTypeId(fallbackTypeId);
                milestoneRepository.save(m);
                log.info("[MilestoneDataFixRunner] Fixed milestone id={} (project={}) -> milestoneTypeId={}",
                        m.getId(), m.getProjectId(), fallbackTypeId);
            }

            log.info("[MilestoneDataFixRunner] Done. Fixed {} milestone(s).", toFix.size());
        } catch (Exception e) {
            // Never let a data-fix task crash application startup.
            log.error("[MilestoneDataFixRunner] Failed to run data fix — leaving data untouched.", e);
        }
    }

    private Long resolveOrCreateFallbackType() {
        Optional<MilestoneType> existing = milestoneTypeRepository.findAll().stream()
                .filter(t -> DEFAULT_TYPE_CODE.equalsIgnoreCase(t.getCode()))
                .findFirst();

        if (existing.isPresent()) {
            return existing.get().getId();
        }

        log.warn("[MilestoneDataFixRunner] No MilestoneType with code '{}' found — creating a default one.",
                DEFAULT_TYPE_CODE);

        MilestoneType type = new MilestoneType();
        type.setCode(DEFAULT_TYPE_CODE);
        type.setLabel(DEFAULT_TYPE_LABEL);
        type.setColor(DEFAULT_TYPE_COLOR);
        type.setLetterCode(DEFAULT_TYPE_LETTER);
        type.setActive(true);
        type = milestoneTypeRepository.save(type);
        return type.getId();
    }
}