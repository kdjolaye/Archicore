package com.klaye.monolith.user.dto;

import java.util.Set;

/**
 * Réponse DTO pour un rôle.
 */
public record RoleResponse(
        String name,
        Set<PermissionResponse> permissions
) {}
