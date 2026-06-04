package com.klaye.monolith.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Requête pour créer ou mettre à jour un rôle.
 */
public record RoleRequest(
        @NotBlank(message = "Le nom du rôle est obligatoire")
        String name,

        @NotEmpty(message = "Au moins une permission est requise")
        Set<String> permissionNames
) {}
