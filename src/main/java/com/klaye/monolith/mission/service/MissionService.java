package com.klaye.monolith.mission.service;

import com.klaye.monolith.audit.entity.AuditAction;
import com.klaye.monolith.audit.service.AuditService;
import com.klaye.monolith.audit.util.AuditUtils;
import com.klaye.monolith.auth.config.JwtUserDetails;
import com.klaye.monolith.dossier.service.DossierService;
import com.klaye.monolith.mission.dto.*;
import com.klaye.monolith.mission.entity.Equipe;
import com.klaye.monolith.mission.entity.Mission;
import com.klaye.monolith.mission.entity.UserMission;
import com.klaye.monolith.mission.mapper.EquipeMapper;
import com.klaye.monolith.mission.mapper.MissionMapper;
import com.klaye.monolith.mission.repository.EquipeRepository;
import com.klaye.monolith.mission.repository.MissionRepository;
import com.klaye.monolith.mission.repository.UserMissionRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final EquipeRepository equipeRepository;
    private final UserMissionRepository userMissionRepository;
    private final DossierService dossierService;
    private final AuditService auditService;

    @Transactional
    public MissionResponse create(MissionCreateRequest request) {
        if (missionRepository.existsByCodeMission(request.codeMission())) {
            throw new ResourceAlreadyExistsException(
                    "Une mission avec le code " + request.codeMission() + " existe déjà.");
        }

        try {
            dossierService.findByCode(request.codeDossier());
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(
                    "Dossier introuvable avec le code Dossier " + request.codeDossier());
        }

        Mission mission = MissionMapper.toEntity(request);
        mission.setCreatedBy(0L); // Placeholder
        mission.setUpdatedBy(0L); // Placeholder

        Mission savedMission = missionRepository.save(mission);

        auditService.log(
                AuditAction.CREER_MISSION,
                AuditUtils.getCurrentUsername(),   // ← utilitaire partagé
                null,
                "Mission",
                request.codeMission(),
                "Création mission : " + request.codeMission(),
                AuditUtils.getClientIp()           // ← utilitaire partagé
        );

        return MissionMapper.toResponse(savedMission);
    }

    public List<MissionResponse> findAll() {
        return missionRepository.findAll().stream()
                .map(MissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<MissionResponse> findAvailableForAdmin() {
        return missionRepository.findAll().stream()
                .filter(m -> m.getStatut() == Mission.StatutMission.PLANIFIEE)
                .filter(m -> userMissionRepository.findByCodeMission(m.getCodeMission()).isEmpty())
                .map(MissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public MissionResponse findByCode(String codeMission) {
        Mission mission = missionRepository.findByCodeMission(codeMission)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mission introuvable avec le code : " + codeMission));
        return MissionMapper.toResponse(mission);
    }

    @Transactional
    public MissionResponse update(String codeMission, MissionUpdateRequest request) {
        Mission mission = missionRepository.findByCodeMission(codeMission)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mission introuvable avec le code : " + codeMission));

        if (request.exercice() != null) mission.setExercice(request.exercice());
        if (request.statut() != null)   mission.setStatut(request.statut());

        mission.setUpdatedBy(0L); // Placeholder
        auditService.log(
                AuditAction.UPDATE_MISSION,
                AuditUtils.getCurrentUsername(),   // ← utilitaire partagé
                null,
                "Mission",
                codeMission,
                "Update mission : " + codeMission,
                AuditUtils.getClientIp()           // ← utilitaire partagé
        );
        return MissionMapper.toResponse(missionRepository.save(mission));
    }

    @Transactional
    public void delete(String codeMission) {
        Mission mission = missionRepository.findByCodeMission(codeMission)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mission introuvable avec le code : " + codeMission));
        auditService.log(
                AuditAction.SUPPRIMER_MISSION,
                AuditUtils.getCurrentUsername(),   // ← utilitaire partagé
                null,
                "Mission",
                codeMission,
                "Supprimer mission : " + codeMission,
                AuditUtils.getClientIp()           // ← utilitaire partagé
        );
        List<Equipe> equipe = equipeRepository.findByCodeMission(mission.getCodeMission());
        equipeRepository.deleteAll(equipe);
        missionRepository.delete(mission);
    }

    /**
     * Ajoute un membre à l'équipe d'une mission.
     */
    @Transactional
    public EquipeResponse addMembreEquipe(EquipeCreateRequest request) {
        if (!missionRepository.existsByCodeMission(request.codeMission())) {
            throw new ResourceNotFoundException(
                    "Mission introuvable avec le code " + request.codeMission());
        }

        if (equipeRepository.existsByCodeMissionAndUserId(request.codeMission(), request.userId())) {
            throw new IllegalStateException("L'utilisateur est déjà membre de cette équipe.");
        }

        Equipe equipe = EquipeMapper.toEntity(request);
        Equipe savedEquipe = equipeRepository.save(equipe);

        auditService.log(
                AuditAction.CONSTITUER_EQUIPE,
                AuditUtils.getCurrentUsername(),
                request.userId(),
                "Mission",
                request.codeMission(),
                "Ajout membre userId=" + request.userId() +
                        " | mission=" + request.codeMission(),
                AuditUtils.getClientIp()
        );

        return EquipeMapper.toResponse(savedEquipe);
    }

    public List<EquipeResponse> getEquipeByMission(String codeMission) {
        return equipeRepository.findByCodeMission(codeMission).stream()
                .map(EquipeMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Gestion des assignations Mission → Admin
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Assigne un utilisateur (ADMIN) à une mission.
     * Seuls le SUPER_ADMIN et l'ADMIN_PRINCIPAL peuvent effectuer cette opération.
     *
     * @param codeMission Code de la mission cible
     * @param userId      ID de l'utilisateur à assigner
     * @param assignedBy  ID de l'utilisateur effectuant l'assignation
     */
    @Transactional
    public UserMissionResponse assignMissionToUser(String codeMission, Long userId, Long assignedBy) {
        Mission mission = missionRepository.findByCodeMission(codeMission)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mission introuvable avec le code : " + codeMission));

        if (userMissionRepository.existsByUserIdAndMissionId(userId, mission.getId())) {
            throw new ResourceAlreadyExistsException(
                    "L'utilisateur " + userId + " est déjà assigné à la mission " + codeMission);
        }

        UserMission userMission = UserMission.builder()
                .userId(userId)
                .missionId(mission.getId())
                .codeMission(codeMission)
                .assignedBy(assignedBy)
                .build();

        UserMission saved = userMissionRepository.save(userMission);

        auditService.log(
                AuditAction.CONSTITUER_EQUIPE,
                AuditUtils.getCurrentUsername(),
                userId,
                "Mission",
                codeMission,
                "Assignation userId=" + userId + " à mission=" + codeMission,
                AuditUtils.getClientIp()
        );

        return toUserMissionResponse(saved);
    }

    /**
     * Retire l'assignation d'un utilisateur sur une mission.
     */
    @Transactional
    public void unassignMissionFromUser(String codeMission, Long userId) {
        if (!userMissionRepository.existsByUserIdAndCodeMission(userId, codeMission)) {
            throw new ResourceNotFoundException(
                    "Assignation introuvable pour userId=" + userId + " et mission=" + codeMission);
        }

        userMissionRepository.deleteByUserIdAndCodeMission(userId, codeMission);

        auditService.log(
                AuditAction.SUPPRIMER_MISSION,
                AuditUtils.getCurrentUsername(),
                userId,
                "Mission",
                codeMission,
                "Retrait assignation userId=" + userId + " de mission=" + codeMission,
                AuditUtils.getClientIp()
        );
    }

    /**
     * Liste tous les utilisateurs assignés à une mission.
     */
    public List<UserMissionResponse> getAssignedUsers(String codeMission) {
        return userMissionRepository.findByCodeMission(codeMission).stream()
                .map(this::toUserMissionResponse)
                .collect(Collectors.toList());
    }

    private UserMissionResponse toUserMissionResponse(UserMission um) {
        return new UserMissionResponse(
                um.getId(),
                um.getUserId(),
                um.getMissionId(),
                um.getCodeMission(),
                um.getAssignedBy(),
                um.getAssignedAt()
        );
    }
    public boolean isCurrentUserAssignedTo(String codeMission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth.getPrincipal() instanceof JwtUserDetails principal)) return false;
        return userMissionRepository.existsByUserIdAndCodeMission(principal.userId(), codeMission);
    }
    public List<String> getCodeMissionsAssignedToCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth.getPrincipal() instanceof JwtUserDetails principal)) return List.of();
        return userMissionRepository.findByUserId(principal.userId())
                .stream()
                .map(UserMission::getCodeMission)
                .collect(Collectors.toList());
    }
    public List<MissionResponse> findMissionsAssignedToUser(Long userId) {
        return userMissionRepository.findByUserId(userId).stream()
                .map(um -> missionRepository.findById(um.getMissionId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(MissionMapper::toResponse)
                .collect(Collectors.toList());
    }
}