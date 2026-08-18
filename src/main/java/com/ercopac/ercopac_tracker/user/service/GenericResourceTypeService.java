package com.ercopac.ercopac_tracker.user.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Guarantees the organisation-scoped fallback planning type without frontend seeding. */
@Service
public class GenericResourceTypeService {
    public static final String CODE = "GENERIC";
    private final ResourceTypeRepository resourceTypes;
    public GenericResourceTypeService(ResourceTypeRepository resourceTypes) { this.resourceTypes = resourceTypes; }
    @Transactional
    public ResourceType ensure(Organisation organisation) {
        return resourceTypes.findByCodeAndOrganisation_Id(CODE, organisation.getId()).orElseGet(() -> {
            ResourceType type = new ResourceType(CODE, "Generic", organisation);
            type.setColour("#6b7280");
            return resourceTypes.save(type);
        });
    }
}
