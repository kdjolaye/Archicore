package com.klaye.monolith.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Requête pour la mise à jour des informations de profil (sans mot de passe).
 * Utilisée par l'endpoint PUT /api/v1/users/me/profile.
 * Séparée de ChangePasswordRequest pour une responsabilité unique.
 */
public record UpdateProfileRequest(
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(min = 2, max = 50)
        String name,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 50)
        String surname,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format de téléphone invalide")
        String phone
) {}
