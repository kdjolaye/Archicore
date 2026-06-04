package com.klaye.monolith.mission.controller;

import com.klaye.monolith.mission.dto.*;
import com.klaye.monolith.mission.service.MissionService;
import com.klaye.monolith.auth.config.JwtUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des Missions d'audit.
 * Migré depuis Mission-Service vers le Monolithe.
 */
@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    /**
     * Crée une nouvelle mission.
     */
    @PostMapping
    @PreAuthorize("hasPermission(null, 'Mission', 'WRITE')")
    public ResponseEntity<MissionResponse> create(@Valid @RequestBody MissionCreateRequest request) {
        // Dans le monolithe, le service récupère l'identifiant utilisateur via le contexte si nécessaire
        return new ResponseEntity<>(missionService.create(request), HttpStatus.CREATED);
    }

    /**
     * Liste toutes les missions.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'Mission', 'READ')")
    public ResponseEntity<List<MissionResponse>> findAll() {
        return ResponseEntity.ok(missionService.findAll());
    }

    /**
     * Liste les missions planifiées sans admin assigné.
     */
    @GetMapping(path = "/available-for-admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'Mission', 'READ') and (hasRole('SUPER_ADMIN') or hasRole('ADMIN_PRINCIPAL'))")
    public ResponseEntity<List<MissionResponse>> getAvailableForAdmin() {
        return ResponseEntity.ok(missionService.findAvailableForAdmin());
    }

    /**
     * Récupère une mission par son code unique.
     */
    @GetMapping(path = "/{code}",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(#code, 'Mission', 'READ')")
    public ResponseEntity<MissionResponse> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(missionService.findByCode(code));
    }

    /**
     * Met à jour une mission.
     */
    @PutMapping("/{code}")
    @PreAuthorize("hasPermission(#code, 'Mission', 'WRITE')")
    public ResponseEntity<MissionResponse> update(@PathVariable String code, @RequestBody MissionUpdateRequest request) {
        return ResponseEntity.ok(missionService.update(code, request));
    }

    /**
     * Supprime une mission.
     */
    @DeleteMapping("/{code}")
    @PreAuthorize("hasPermission(#code, 'Mission', 'DELETE')")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        missionService.delete(code);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ajoute un membre à l'équipe d'une mission.
     */
    @PostMapping("/{code}/equipe")
    @PreAuthorize("hasPermission(#code, 'Mission', 'WRITE')")
    public ResponseEntity<EquipeResponse> addMembreEquipe(@PathVariable String code, @Valid @RequestBody EquipeCreateRequest request) {
        return new ResponseEntity<>(missionService.addMembreEquipe(request), HttpStatus.CREATED);
    }

    /**
     * Récupère l'équipe d'une mission.
     */
    @GetMapping(path = "/{code}/equipe",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(#code, 'Mission', 'READ')")
    public ResponseEntity<List<EquipeResponse>> getEquipeByMission(@PathVariable String code) {
        return ResponseEntity.ok(missionService.getEquipeByMission(code));
    }

    /**
     * Assigne un utilisateur (ADMIN) à une mission.
     */
    @PostMapping("/{code}/assign")
    @PreAuthorize("hasPermission(null, 'Mission', 'WRITE')")
    public ResponseEntity<UserMissionResponse> assignUser(
            @PathVariable String code,
            @Valid @RequestBody UserMissionAssignRequest request,
            Authentication authentication) {
        
        JwtUserDetails principal = (JwtUserDetails) authentication.getPrincipal();
        return new ResponseEntity<>(missionService.assignMissionToUser(code, request.userId(), principal.userId()), HttpStatus.CREATED);
    }

    /**
     * Retire l'assignation d'un utilisateur sur une mission.
     */
    @DeleteMapping("/{code}/assign/{userId}")
    @PreAuthorize("hasPermission(null, 'Mission', 'WRITE')")
    public ResponseEntity<Void> unassignUser(@PathVariable String code, @PathVariable Long userId) {
        missionService.unassignMissionFromUser(code, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Liste les utilisateurs assignés à une mission.
     */
    @GetMapping(value = "/{code}/assign",produces = MediaType.APPLICATION_JSON_VALUE)
    // ─── Restreindre getAssignedUsers aux super admins ───────────────────
    @PreAuthorize("hasPermission(null, 'Mission', 'READ') and (hasRole('SUPER_ADMIN') or hasRole('ADMIN_PRINCIPAL'))")
    public ResponseEntity<List<UserMissionResponse>> getAssignedUsers(@PathVariable String code) {
        return ResponseEntity.ok(missionService.getAssignedUsers(code));
    }

    // ─── Nouvel endpoint : l'utilisateur courant est-il assigné ? ────────
    @GetMapping(value = "/{code}/assigned-me",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(#code, 'Mission', 'READ')")
    public ResponseEntity<Boolean> isAssignedToMe(@PathVariable String code) {
        return ResponseEntity.ok(missionService.isCurrentUserAssignedTo(code));
    }
    @GetMapping(value = "/my-missions",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'Mission', 'READ')")
    public ResponseEntity<List<String>> getMyAssignedMissions() {
        return ResponseEntity.ok(missionService.getCodeMissionsAssignedToCurrentUser());
    }

    /**
     * Liste les missions assignées à un utilisateur spécifique.
     */
    @GetMapping(path = "/assigned-to-user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN_PRINCIPAL')")
    public ResponseEntity<List<MissionResponse>> getMissionsAssignedToUser(@PathVariable Long userId) {
        return ResponseEntity.ok(missionService.findMissionsAssignedToUser(userId));
    }
}
