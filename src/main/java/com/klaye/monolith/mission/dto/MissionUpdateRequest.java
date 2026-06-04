package com.klaye.monolith.mission.dto;

import com.klaye.monolith.mission.entity.Mission;

/**
 * Requête de mise à jour d'une mission.
 */
public record MissionUpdateRequest(
        Mission.StatutMission statut,
        String exercice) {
}
