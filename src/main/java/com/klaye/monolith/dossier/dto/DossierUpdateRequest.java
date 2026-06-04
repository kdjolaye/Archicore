package com.klaye.monolith.dossier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête de mise à jour d'un dossier client existant.
 */
public record DossierUpdateRequest(
        @NotBlank(message = "La raison sociale est obligatoire")
        String raisonSociale,

        String typeEntreprise,
        String adresse,
        String phone,

        @Email(message = "Format email invalide")
        String email,

        String bp
) {}
