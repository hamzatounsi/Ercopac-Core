package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Gantt resource picker: only active internal employees; the type always comes from the configured resource type
class ResourceClassificationTest {

    private AppUser user(boolean internal, boolean active, Role role) {
        AppUser u = new AppUser();
        u.setInternalUser(internal);
        u.setActive(active);
        u.setRoles(Set.of(role));
        return u;
    }

    @Test
    void externalAndClientAccountsAreNotInternalEmployees() {
        assertThat(ResourceClassification.isInternalEmployee(user(true, true, Role.EMPLOYEE))).isTrue();
        assertThat(ResourceClassification.isInternalEmployee(user(false, true, Role.EMPLOYEE))).isFalse();
        assertThat(ResourceClassification.isInternalEmployee(user(true, false, Role.EMPLOYEE))).isFalse();
        assertThat(ResourceClassification.isInternalEmployee(user(true, true, Role.CLIENT))).isFalse();
    }

    @Test
    void resourceTypeComesFromTheConfiguredType() {
        AppUser u = user(true, true, Role.EMPLOYEE);
        assertThat(ResourceClassification.resourceTypeCode(u)).isNull();
        u.setResourceType(new ResourceType("ELEC", "Electrician", null));
        assertThat(ResourceClassification.resourceTypeCode(u)).isEqualTo("ELEC");
    }
}
