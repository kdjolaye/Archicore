package com.klaye.monolith.mission.mapper;

import com.klaye.monolith.mission.dto.MissionCreateRequest;
import com.klaye.monolith.mission.dto.MissionResponse;
import com.klaye.monolith.mission.entity.Mission;

/**
 * Mapper pour la conversion entre Entité Mission et DTOs.
 */
public class MissionMapper {

    /**
     * Convertit une requête de création en entité Mission.
     */
    public static Mission toEntity(MissionCreateRequest request) {
        if (request == null)
            return null;
        return Mission.builder()
                .codeMission(request.codeMission())
                .exercice(request.exercice())
                .statut(Mission.StatutMission.PLANIFIEE)
                .codeDossier(request.codeDossier())
                .build();
    }

    /**
     * Convertit une entité Mission en réponse DTO.
     */
    public static MissionResponse toResponse(Mission mission) {
        if (mission == null)
            return null;
        return new MissionResponse(
                mission.getCodeMission(),
                mission.getExercice(),
                mission.getCodeDossier(),
                mission.getStatut(),
                mission.getCreatedAt()
               );
    }
}
