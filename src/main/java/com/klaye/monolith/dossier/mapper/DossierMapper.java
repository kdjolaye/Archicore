package com.klaye.monolith.dossier.mapper;

import com.klaye.monolith.dossier.dto.DossierCreateRequest;
import com.klaye.monolith.dossier.dto.DossierResponse;
import com.klaye.monolith.dossier.entity.Dossier;
import org.springframework.stereotype.Component;

/**
 * Mapper pour la conversion entre l'entité Dossier et ses DTOs.
 */
@Component
public class DossierMapper {

    /**
     * Convertit une requête de création en entité Dossier.
     */
    public Dossier toEntity(DossierCreateRequest request) {
        if (request == null) return null;
        return Dossier.builder()
                .codeDossier(request.codeDossier())
                .raisonSociale(request.raisonSociale())
                .typeEntreprise(request.typeEntreprise())
                .adresse(request.adresse())
                .phone(request.phone())
                .email(request.email())
                .bp(request.bp())
                .build();
    }

    /**
     * Convertit une entité Dossier en réponse DTO.
     */
    public DossierResponse toResponse(Dossier dossier) {
        if (dossier == null) return null;
        return new DossierResponse(
                dossier.getCodeDossier(),
                dossier.getRaisonSociale(),
                dossier.getTypeEntreprise(),
                dossier.getAdresse(),
                dossier.getPhone(),
                dossier.getEmail(),
                dossier.getBp()
        );
    }
}
