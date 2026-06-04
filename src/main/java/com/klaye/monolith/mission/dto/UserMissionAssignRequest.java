package com.klaye.monolith.mission.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO pour assigner un utilisateur (ADMIN) à une mission.
 */
public record UserMissionAssignRequest(
        @NotNull(message = "L'ID de l'utilisateur est obligatoire")
        Long userId
) {}
