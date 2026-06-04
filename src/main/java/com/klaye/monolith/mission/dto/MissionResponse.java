package com.klaye.monolith.mission.dto;

import com.klaye.monolith.mission.entity.Mission;
import java.time.LocalDateTime;

/**
 * Réponse contenant les informations d'une mission.
 */
public record MissionResponse(
        String codeMission,
        String exercice,
        String codeDossier,
        Mission.StatutMission statut,
        LocalDateTime createdAt){
}
