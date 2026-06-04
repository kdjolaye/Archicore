package com.klaye.monolith.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Requête pour le changement de mot de passe.
 * Utilisée par l'endpoint PATCH /api/v1/users/me/password.
 * Couvre deux scénarios :
 *   - Première connexion (FORCE_PASSWORD_CHANGE) : currentPassword = mot de passe temporaire
 *   - Expiration mensuelle (PASSWORD_EXPIRED)     : currentPassword = ancien mot de passe
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        String currentPassword,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial"
        )
        String newPassword
) {}
