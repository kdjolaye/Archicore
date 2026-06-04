package com.klaye.monolith.user.dto;

import jakarta.validation.constraints.*;

/**
 * Requête de création d'un utilisateur par un Administrateur.
 */
public record AdminUserCreateRequest(
        @NotBlank @Email String username,
        @NotNull String roleName
) {}
