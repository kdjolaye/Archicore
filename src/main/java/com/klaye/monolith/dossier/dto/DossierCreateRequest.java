package com.klaye.monolith.dossier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création d'un nouveau dossier client.
 */
public record DossierCreateRequest(
        @NotBlank(message = "Le code dossier est obligatoire")
        String codeDossier,

        @NotBlank(message = "La raison sociale est obligatoire")
        String raisonSociale,

        String typeEntreprise,
        String adresse,
        String phone,

        @Email(message = "Format email invalide")
        String email,

        String bp // Boîte Postale
) {}
