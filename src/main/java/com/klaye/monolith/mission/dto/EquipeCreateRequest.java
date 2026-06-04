package com.klaye.monolith.mission.dto;

import com.klaye.monolith.mission.entity.Equipe;
import jakarta.validation.constraints.NotNull;

/**
 * Requête pour ajouter un membre à une équipe de mission.
 */
public record EquipeCreateRequest(
        @NotNull(message = "Le code de la mission est obligatoire")
        String codeMission,

        @NotNull(message = "L'ID de l'utilisateur est obligatoire")
        Long userId,

        @NotNull(message = "Le poste est obligatoire")
        Equipe.Poste poste) {
}
