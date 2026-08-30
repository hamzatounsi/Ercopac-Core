package com.ercopac.ercopac_tracker.milestone.service;

import com.ercopac.ercopac_tracker.milestone.domain.MilestoneType;
import com.ercopac.ercopac_tracker.milestone.dto.MilestoneTypeDto;
import com.ercopac.ercopac_tracker.milestone.repository.MilestoneTypeRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.service.ProjectAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MilestoneTypeService {

    private final MilestoneTypeRepository milestoneTypeRepository;
    private final ProjectAccessService projectAccessService;

    public MilestoneTypeService(MilestoneTypeRepository milestoneTypeRepository,
                                ProjectAccessService projectAccessService) {
        this.milestoneTypeRepository = milestoneTypeRepository;
        this.projectAccessService = projectAccessService;
    }

    public List<MilestoneTypeDto> getMilestoneTypes(Long projectId) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        ensureDefaultMilestoneTypes(project);
        return milestoneTypeRepository.findByProjectIdAndOrganisation_IdAndActiveTrueOrderByCodeAsc(
                        projectId, project.getOrganisation().getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public MilestoneTypeDto createMilestoneType(Long projectId, MilestoneTypeDto dto) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        String code = requireText(dto.getCode(), "Milestone name is required.");
        String label = requireText(dto.getLabel(), "Milestone label is required.");
        if (milestoneTypeRepository.existsByProjectIdAndLabelIgnoreCase(projectId, label)) {
            throw new IllegalArgumentException("A milestone with this name already exists in this project.");
        }

        MilestoneType type = new MilestoneType();
        type.setProjectId(projectId);
        type.setCode(code);
        type.setLabel(label);
        type.setColor(requireColor(dto.getColor()));
        type.setLetterCode(dto.getLetterCode());
        type.setOrganisation(project.getOrganisation());
        type.setActive(true);

        return toDto(milestoneTypeRepository.save(type));
    }

    public MilestoneTypeDto updateMilestoneType(Long projectId, Long id, MilestoneTypeDto dto) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        MilestoneType type = milestoneTypeRepository.findByIdAndProjectIdAndOrganisation_Id(id, projectId, project.getOrganisation().getId())
            .orElseThrow(() -> new IllegalArgumentException("Milestone type not found"));

        type.setCode(requireText(dto.getCode(), "Milestone name is required."));
        type.setLabel(requireText(dto.getLabel(), "Milestone label is required."));
        type.setColor(requireColor(dto.getColor()));
        type.setLetterCode(dto.getLetterCode());
        type.setActive(dto.isActive());

        return toDto(milestoneTypeRepository.save(type));
    }

    public void deleteMilestoneType(Long projectId, Long id) {
        Project project = projectAccessService.getAccessibleProject(projectId);
        MilestoneType type = milestoneTypeRepository.findByIdAndProjectIdAndOrganisation_Id(id, projectId, project.getOrganisation().getId())
                .orElseThrow(() -> new IllegalArgumentException("Milestone type not found"));
        type.setActive(false);
        milestoneTypeRepository.save(type);
    }

    private MilestoneTypeDto toDto(MilestoneType type) {
        MilestoneTypeDto dto = new MilestoneTypeDto();
        dto.setId(type.getId());
        dto.setProjectId(type.getProjectId());
        dto.setCode(type.getCode());
        dto.setLabel(type.getLabel());
        dto.setColor(type.getColor());
        dto.setLetterCode(type.getLetterCode());
        dto.setActive(type.isActive());
        return dto;
    }

    /** Creates only missing definitions; it never schedules milestone events. */
    public void ensureDefaultMilestoneTypes(Project project) {
        for (DefaultMilestone definition : DEFAULT_MILESTONES) {
            var existing = milestoneTypeRepository.findByProjectIdAndLabelIgnoreCase(project.getId(), definition.label());
            if (existing.isPresent()) {
                if (!existing.get().isActive()) {
                    existing.get().setActive(true);
                    existing.get().setColor(definition.color());
                    existing.get().setCode(definition.code());
                    milestoneTypeRepository.save(existing.get());
                }
                continue;
            }
            MilestoneType type = new MilestoneType();
            type.setProjectId(project.getId());
            type.setCode(definition.code());
            type.setLabel(definition.label());
            type.setColor(definition.color());
            type.setLetterCode(definition.code());
            type.setOrganisation(project.getOrganisation());
            type.setActive(true);
            milestoneTypeRepository.save(type);
        }
    }

    private record DefaultMilestone(String label, String code, String color) {}
    private static final List<DefaultMilestone> DEFAULT_MILESTONES = List.of(
            new DefaultMilestone("RT", "RT", "#7FFFD4"),
            new DefaultMilestone("KOM", "KO", "#228B22"),
            new DefaultMilestone("DISTINTA UTM", "BM", "#FF5C8A"),
            new DefaultMilestone("DISTINTA UTE", "BE", "#FFF59D"),
            new DefaultMilestone("APPROVVIGIONAMENTO", "PR", "#FFDAB9"),
            new DefaultMilestone("MONTAGGIO INTERNO", "MI", "#F59E0B"),
            new DefaultMilestone("COLLAUDO INTERNO", "CI", "#6B3A2A"),
            new DefaultMilestone("FAT", "F", "#A21CAF"),
            new DefaultMilestone("SPEDIZIONE", "SP", "#FDE047"),
            new DefaultMilestone("INSTALLAZIONE", "I", "#22D3EE"),
            new DefaultMilestone("AVVIAMENTO E COLLAUDO", "CI", "#115E59"),
            new DefaultMilestone("TRAINING", "T", "#C026D3"),
            new DefaultMilestone("MESSA IN SERVIZIO", "GL", "#A5F3FC"),
            new DefaultMilestone("SAT", "S", "#9333EA")
    );

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private String requireColor(String value) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("A valid milestone colour is required.");
        return value.toUpperCase();
    }
}
