package com.klaye.monolith.dossier.service;

import com.klaye.monolith.audit.entity.AuditAction;
import com.klaye.monolith.audit.service.AuditService;
import com.klaye.monolith.audit.util.AuditUtils;
import com.klaye.monolith.dossier.dto.DossierCreateRequest;
import com.klaye.monolith.dossier.dto.DossierUpdateRequest;
import com.klaye.monolith.dossier.dto.DossierResponse;
import com.klaye.monolith.dossier.entity.Dossier;
import com.klaye.monolith.dossier.mapper.DossierMapper;
import com.klaye.monolith.dossier.repository.DossierRepository;
import com.klaye.monolith.common.exception.ResourceAlreadyExistsException;
import com.klaye.monolith.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DossierService {

    private final DossierRepository dossierRepository;
    private final DossierMapper dossierMapper;
    private final AuditService auditService;

    @Transactional
    public DossierResponse create(DossierCreateRequest request) {
        if (dossierRepository.existsByCodeDossier(request.codeDossier())) {
            throw new ResourceAlreadyExistsException(
                    "Un dossier avec le code '" + request.codeDossier() + "' existe déjà");
        }

        Dossier dossier = dossierMapper.toEntity(request);
        dossier.setCreatedBy(0L); // Placeholder

        DossierResponse response = dossierMapper.toResponse(dossierRepository.save(dossier));

        auditService.log(
                AuditAction.CREER_DOSSIER,
                AuditUtils.getCurrentUsername(),   // ← utilitaire partagé
                null,
                "Dossier",
                request.codeDossier(),
                "Création dossier : " + request.codeDossier(),
                AuditUtils.getClientIp()           // ← utilitaire partagé
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> findAll() {
        return dossierRepository.findAll()
                .stream()
                .map(dossierMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DossierResponse findByCode(String codeDossier) {
        return dossierRepository.findByCodeDossier(codeDossier)
                .map(dossierMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dossier introuvable : " + codeDossier));
    }

    @Transactional
    public DossierResponse update(String codeDossier, DossierUpdateRequest request) {
        Dossier dossier = dossierRepository.findByCodeDossier(codeDossier)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dossier introuvable : " + codeDossier));

        dossier.setRaisonSociale(request.raisonSociale());
        dossier.setTypeEntreprise(request.typeEntreprise());
        dossier.setAdresse(request.adresse());
        dossier.setPhone(request.phone());
        dossier.setEmail(request.email());
        dossier.setBp(request.bp());
        dossier.setUpdatedBy(0L); // Placeholder
        auditService.log(
                AuditAction.UPDATE_DOSSIER,
                AuditUtils.getCurrentUsername(),   // ← utilitaire partagé
                null,
                "Dossier",
                codeDossier,
                "Update dossier : " + codeDossier,
                AuditUtils.getClientIp()           // ← utilitaire partagé
        );
        return dossierMapper.toResponse(dossierRepository.save(dossier));
    }

    @Transactional
    public void delete(String codeDossier) {
        auditService.log(
                AuditAction.SUPPRIMER_DOSSIER,
                AuditUtils.getCurrentUsername(),   // ← utilitaire partagé
                null,
                "Dossier",
                codeDossier,
                "Supprimer dossier : " + codeDossier,
                AuditUtils.getClientIp()           // ← utilitaire partagé
        );
        Dossier dossier = dossierRepository.findByCodeDossier(codeDossier)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dossier introuvable : " + codeDossier));
        dossierRepository.delete(dossier);
    }


}