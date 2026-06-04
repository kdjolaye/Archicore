package com.klaye.monolith.dossier.dto;

/**
 * Réponse contenant les informations publiques d'un dossier.
 */
public record DossierResponse (
    String codeDossier,
    String raisonSociale,
    String typeEntreprise,
    String adresse,
    String phone,
    String email,
    String bp)
{}
