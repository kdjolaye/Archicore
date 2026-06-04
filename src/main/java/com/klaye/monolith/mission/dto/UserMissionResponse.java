package com.klaye.monolith.mission.dto;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour une assignation Mission → Utilisateur.
 */
public record UserMissionResponse(
        Long id,
        Long userId,
        Long missionId,
        String codeMission,
        Long assignedBy,
        LocalDateTime assignedAt
) {}
