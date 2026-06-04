package com.klaye.monolith.mission.dto;

import com.klaye.monolith.mission.entity.Equipe;

/**
 * Réponse contenant les informations d'un membre d'équipe.
 */
public record EquipeResponse(
        Long id,
        String codeMission,
        Long userId,
        Equipe.Poste poste) {
}
