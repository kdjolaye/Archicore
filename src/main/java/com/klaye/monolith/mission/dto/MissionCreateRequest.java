package com.klaye.monolith.mission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête de création d'une nouvelle mission.
 */
public record MissionCreateRequest(
        @NotBlank(message = "Le code mission est obligatoire") String codeMission,

        @NotBlank(message = "L'exercice est obligatoire") String exercice,

        @NotNull(message = "Le code du dossier est obligatoire") String codeDossier) {
}
