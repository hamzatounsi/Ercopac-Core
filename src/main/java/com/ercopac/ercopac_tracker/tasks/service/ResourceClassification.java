package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;

/**
 * Single place deciding how a resource is classified for planning (Gantt resource picker, task assignments).
 * The resource type always comes from the resource's configured ResourceType (AppUser.resourceType), never from a
 * copied string, so a type change is reflected everywhere.
 *
 * Suppliers are external resources (Supplier entity, assigned through supplier rows) and must never be listed as
 * internal employees.
 */
public final class ResourceClassification {
    private ResourceClassification() {}

    /** Active internal employee that can be picked as an internal resource. */
    public static boolean isInternalEmployee(AppUser u) {
        // TODO(confirm with Anis): exact supplier/external behaviour is pending. For now accounts flagged
        // internal_user = false are treated as external (supplier side) and excluded from the internal list;
        // CLIENT portal accounts are never resources.
        return u != null && u.isActive() && u.isInternalUser() && !u.hasRole(Role.CLIENT);
    }

    /** Configured resource type code of a user, or null. */
    public static String resourceTypeCode(AppUser u) {
        return u == null || u.getResourceType() == null ? null : u.getResourceType().getCode();
    }
}
