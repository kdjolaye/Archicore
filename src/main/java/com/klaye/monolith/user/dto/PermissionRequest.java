package com.klaye.monolith.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête pour créer une nouvelle permission.
 */
public record PermissionRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String name
) {}
