package com.klaye.monolith.mission.mapper;

import com.klaye.monolith.mission.dto.EquipeCreateRequest;
import com.klaye.monolith.mission.dto.EquipeResponse;
import com.klaye.monolith.mission.entity.Equipe;

/**
 * Mapper pour la conversion entre Entité Equipe et DTOs.
 */
public class EquipeMapper {

    /**
     * Convertit une requête de création en entité Equipe.
     */
    public static Equipe toEntity(EquipeCreateRequest request) {
        if (request == null)
            return null;
        return Equipe.builder()
                .codeMission(request.codeMission())
                .userId(request.userId())
                .poste(request.poste())
                .build();
    }

    /**
     * Convertit une entité Equipe en réponse DTO.
     */
    public static EquipeResponse toResponse(Equipe equipe) {
        if (equipe == null)
            return null;
        return new EquipeResponse(
                equipe.getId(),
                equipe.getCodeMission(),
                equipe.getUserId(),
                equipe.getPoste());
    }
}
