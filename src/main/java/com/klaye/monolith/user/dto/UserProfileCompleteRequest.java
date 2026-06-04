package com.klaye.monolith.user.dto;

import jakarta.validation.constraints.*;

/**
 * Requête pour compléter le profil d'un utilisateur après sa première connexion.
 */
public record UserProfileCompleteRequest(
        @NotBlank @Size(min = 2, max = 50) String name,
        @NotBlank @Size(min = 2, max = 50) String surname,
        @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")
        String newPassword
) {}
