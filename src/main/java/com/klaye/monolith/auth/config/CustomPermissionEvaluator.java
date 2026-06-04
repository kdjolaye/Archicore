package com.klaye.monolith.auth.config;

import com.klaye.monolith.mission.repository.UserMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Évaluateur de permissions personnalisé pour le système à 4 rôles.
 *
 * Logique :
 * - SUPER_ADMIN / ADMIN_PRINCIPAL → accès total (return true)
 * - CONSULTANT → lecture seule (READ uniquement)
 * - ADMIN → lecture partout + écriture/suppression uniquement sur ses missions assignées
 *
 * Utilisé via @PreAuthorize("hasPermission(#target, 'Type', 'PERMISSION')")
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserMissionRepository userMissionRepository;

    /**
     * Méthode principale appelée par Spring Security via hasPermission().
     *
     * @param auth               L'authentification courante (contient le principal JwtUserDetails + authorities)
     * @param targetDomainObject Le codeMission (String) ou null si pas de cible spécifique
     * @param permission         L'action demandée : "READ", "WRITE", "DELETE"
     */
    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null || permission == null) {
            log.warn(">>> PermissionEvaluator : auth ou permission null → accès refusé");
            return false;
        }

        String role = extractRole(auth);
        String perm = permission.toString().toUpperCase();

        log.debug(">>> PermissionEvaluator : rôle={}, permission={}, target={}",
                role, perm, targetDomainObject);

        // ─── SUPER_ADMIN & ADMIN_PRINCIPAL : accès total ────────────────
        if ("SUPER_ADMIN".equals(role) || "ADMIN_PRINCIPAL".equals(role)) {
            return true;
        }

        // ─── CONSULTANT : lecture seule stricte ─────────────────────────
        if ("CONSULTANT".equals(role)) {
            return "READ".equals(perm);
        }

        // ─── ADMIN : lecture partout + écriture sur ses missions ─────────
        if ("ADMIN".equals(role)) {
            // Lecture autorisée partout
            if ("READ".equals(perm)) {
                return true;
            }

            // Écriture/Suppression/UPDATE : vérifier l'assignation à la mission
            if ("UPDATE".equals(perm) || "WRITE".equals(perm) || "DELETE".equals(perm)) {
                return isAdminAssignedToMission(auth, targetDomainObject);
            }
        }

        // ─── Rôle inconnu → refusé ──────────────────────────────────────
        log.warn(">>> PermissionEvaluator : rôle inconnu '{}' → accès refusé", role);
        return false;
    }

    /**
     * Surcharge avec targetType (ex: "Mission", "Dossier").
     * Appelée via @PreAuthorize("hasPermission(#id, 'Mission', 'WRITE')")
     * Délègue vers la méthode principale.
     */
    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId,
                                 String targetType, Object permission) {
        return hasPermission(auth, targetId, permission);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Méthodes utilitaires privées
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Extrait le nom du rôle depuis les authorities (cherche le préfixe ROLE_).
     */
    private String extractRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .map(a -> a.substring(5)) // Enlever "ROLE_"
                .orElse("UNKNOWN");
    }

    /**
     * Vérifie si l'ADMIN est assigné à la mission ciblée via la table user_missions.
     */
    private boolean isAdminAssignedToMission(Authentication auth, Object targetDomainObject) {
        if (targetDomainObject == null) {
            log.debug(">>> PermissionEvaluator : pas de mission ciblée → accès refusé pour WRITE/DELETE");
            return false;
        }

        // Extraire le userId depuis le principal JwtUserDetails
        if (!(auth.getPrincipal() instanceof JwtUserDetails principal)) {
            log.warn(">>> PermissionEvaluator : principal n'est pas JwtUserDetails → accès refusé");
            return false;
        }

        Long userId = principal.userId();
        String codeMission = targetDomainObject.toString();

        boolean assigned = userMissionRepository.existsByUserIdAndCodeMission(userId, codeMission);
        log.debug(">>> PermissionEvaluator : userId={}, codeMission={}, assigné={}",
                userId, codeMission, assigned);

        return assigned;
    }
}
