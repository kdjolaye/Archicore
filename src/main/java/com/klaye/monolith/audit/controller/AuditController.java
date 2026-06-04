package com.klaye.monolith.audit.controller;

import com.klaye.monolith.audit.entity.AuditAction;
import com.klaye.monolith.audit.entity.AuditLog;
import com.klaye.monolith.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasPermission(null, 'AuditLog', 'READ')")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    /**
     * Tout l'historique d'une ressource précise.
     * Ex : GET /api/v1/audit/ressource?type=Document&id=abc-uuid
     */
    @GetMapping(value = "/ressource",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuditLog>> getByRessource(
            @RequestParam String type,
            @RequestParam String id) {

        List<AuditLog> logs = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByPerformedAtDesc(type, id);

        return ResponseEntity.ok(logs);
    }

    /**
     * Tout ce qu'a fait un utilisateur précis.
     * Ex : GET /api/v1/audit/utilisateur?username=jean@klaye.com
     */
    @GetMapping(value = "/utilisateur",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuditLog>> getByUtilisateur(
            @RequestParam String username) {

        List<AuditLog> logs = auditLogRepository
                .findByPerformedByOrderByPerformedAtDesc(username);

        return ResponseEntity.ok(logs);
    }

    /**
     * Tous les logs d'un type d'action précis.
     * Ex : GET /api/v1/audit/action?action=SUPPRIMER_DOCUMENT
     */
    @GetMapping(value = "/action",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuditLog>> getByAction(
            @RequestParam AuditAction action) {

        List<AuditLog> logs = auditLogRepository
                .findByActionOrderByPerformedAtDesc(action);

        return ResponseEntity.ok(logs);
    }

    /**
     * Tous les logs entre deux dates.
     * Ex : GET /api/v1/audit/periode?debut=2026-01-01&fin=2026-04-30
     */
    @GetMapping(value = "/periode",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuditLog>> getByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        Instant from = debut.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to   = fin.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<AuditLog> logs = auditLogRepository
                .findByPerformedAtBetweenOrderByPerformedAtDesc(from, to);

        return ResponseEntity.ok(logs);
    }

    /**
     * Tous les logs sans filtre — vue globale.
     * Ex : GET /api/v1/audit/tous
     */
    @GetMapping(value = "/tous",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuditLog>> getTous() {
        List<AuditLog> logs = auditLogRepository
                .findAllByOrderByPerformedAtDesc();

        return ResponseEntity.ok(logs);
    }
}