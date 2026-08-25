package com.ercopac.ercopac_tracker.milestone.service;

import com.ercopac.ercopac_tracker.milestone.domain.MilestoneType;
import com.ercopac.ercopac_tracker.milestone.dto.MilestoneTypeDto;
import com.ercopac.ercopac_tracker.milestone.repository.MilestoneTypeRepository;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MilestoneTypeService {

    private final MilestoneTypeRepository milestoneTypeRepository;
    private final OrganisationRepository organisationRepository;
    private final SecurityUtils securityUtils;

    public MilestoneTypeService(MilestoneTypeRepository milestoneTypeRepository,
                                OrganisationRepository organisationRepository,
                                SecurityUtils securityUtils) {
        this.milestoneTypeRepository = milestoneTypeRepository;
        this.organisationRepository = organisationRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<MilestoneTypeDto> getAllMilestoneTypes() {
        Long orgId = securityUtils.getCurrentOrganisationId();
        List<MilestoneType> types;
        
        if (securityUtils.isPlatformUser()) {
            types = milestoneTypeRepository.findByActiveTrueOrderByCodeAsc();
        } else {
            types = milestoneTypeRepository.findByOrganisationIdAndActiveTrueOrderByCodeAsc(orgId);
        }
        
        return types.stream().map(this::toDto).collect(Collectors.toList());
    }

    public MilestoneTypeDto createMilestoneType(MilestoneTypeDto dto) {
        Long orgId = securityUtils.getCurrentOrganisationId();
        Organisation org = organisationRepository.findById(orgId)
            .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        MilestoneType type = new MilestoneType();
        type.setCode(dto.getCode());
        type.setLabel(dto.getLabel());
        type.setColor(dto.getColor());
        type.setLetterCode(dto.getLetterCode());
        type.setOrganisation(org);
        type.setActive(true);

        return toDto(milestoneTypeRepository.save(type));
    }

    public MilestoneTypeDto updateMilestoneType(Long id, MilestoneTypeDto dto) {
        MilestoneType type = milestoneTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Milestone type not found"));

        type.setCode(dto.getCode());
        type.setLabel(dto.getLabel());
        type.setColor(dto.getColor());
        type.setLetterCode(dto.getLetterCode());
        type.setActive(dto.isActive());

        return toDto(milestoneTypeRepository.save(type));
    }

    public void deleteMilestoneType(Long id) {
        milestoneTypeRepository.deleteById(id);
    }

    private MilestoneTypeDto toDto(MilestoneType type) {
        MilestoneTypeDto dto = new MilestoneTypeDto();
        dto.setId(type.getId());
        dto.setCode(type.getCode());
        dto.setLabel(type.getLabel());
        dto.setColor(type.getColor());
        dto.setLetterCode(type.getLetterCode());
        dto.setActive(type.isActive());
        return dto;
    }
}