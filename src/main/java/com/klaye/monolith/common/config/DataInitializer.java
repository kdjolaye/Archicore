package com.klaye.monolith.common.config;

import com.klaye.monolith.user.entity.Permission;
import com.klaye.monolith.user.entity.Role;
import com.klaye.monolith.user.entity.User;
import com.klaye.monolith.user.repository.PermissionRepository;
import com.klaye.monolith.user.repository.RoleRepository;
import com.klaye.monolith.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Composant de démarrage qui initialise les données de référence en base.
 * S'exécute au lancement de l'application Spring Boot.
 * Ne crée les données que si elles n'existent pas encore (idempotent).
 *
 * Système à 4 rôles :
 * - SUPER_ADMIN     : Accès total, crée les ADMIN_PRINCIPAL
 * - ADMIN_PRINCIPAL : Accès total, crée les Dossiers/Missions/Admins
 * - ADMIN           : CRUD sur ses missions assignées, lecture sur le reste
 * - CONSULTANT      : Lecture seule stricte
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info(">>> Initialisation des données de référence...");

        // ─── Étape 1 : Permissions ────────────────────────────────────────
        List<String> permissionNames = Arrays.asList(
                "USER_READ", "USER_UPDATE",
                "DOSSIER_CREATE", "DOSSIER_READ", "DOSSIER_UPDATE", "DOSSIER_DELETE",
                "MISSION_CREATE", "MISSION_READ", "MISSION_UPDATE", "MISSION_DELETE",
                "DOCUMENT_CREATE", "DOCUMENT_READ", "DOCUMENT_DELETE",
                "PERMISSIONS_MANAGE"
        );

        permissionNames.forEach(name -> {
            if (permissionRepository.findByName(name).isEmpty()) {
                permissionRepository.save(Permission.builder().name(name).build());
                log.info(">>> Permission créée : {}", name);
            }
        });

        // ─── Étape 2 : Rôle SUPER_ADMIN — bypass total ──────────────────
        if (roleRepository.findByName("SUPER_ADMIN").isEmpty()) {
            Role superAdminRole = Role.builder()
                    .name("SUPER_ADMIN")
                    .permissions(new HashSet<>())
                    .build();
            roleRepository.save(superAdminRole);
            log.info(">>> Rôle créé : SUPER_ADMIN (accès total via PermissionEvaluator)");
        }

        // ─── Étape 3 : Rôle ADMIN_PRINCIPAL — bypass total ──────────────
        if (roleRepository.findByName("ADMIN_PRINCIPAL").isEmpty()) {
            Role adminPrincipalRole = Role.builder()
                    .name("ADMIN_PRINCIPAL")
                    .permissions(new HashSet<>())
                    .build();
            roleRepository.save(adminPrincipalRole);
            log.info(">>> Rôle créé : ADMIN_PRINCIPAL (accès total via PermissionEvaluator)");
        }

        // ─── Étape 4 : Rôle ADMIN ───────────────────────────────────────
        // Permissions de base pour la lecture. L'écriture/suppression sur
        // les missions assignées est gérée par le PermissionEvaluator.
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Set<Permission> adminPermissions = new HashSet<>();
            List<String> adminPerms = Arrays.asList(
                    "DOSSIER_READ", "MISSION_READ","MISSION_UPDATE",
                    "DOCUMENT_READ", "DOCUMENT_CREATE","DOCUMENT_DELETE"
            );
            adminPerms.forEach(p ->
                    permissionRepository.findByName(p).ifPresent(adminPermissions::add));

            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .permissions(adminPermissions)
                    .build();
            roleRepository.save(adminRole);
            log.info(">>> Rôle créé : ADMIN avec {} permissions", adminPermissions.size());
        }

        // ─── Étape 5 : Rôle CONSULTANT — lecture seule stricte ──────────
        if (roleRepository.findByName("CONSULTANT").isEmpty()) {
            Set<Permission> consultantPermissions = new HashSet<>();
            List<String> consultantPerms = Arrays.asList(
                    "DOSSIER_READ", "MISSION_READ", "DOCUMENT_READ"
            );
            consultantPerms.forEach(p ->
                    permissionRepository.findByName(p).ifPresent(consultantPermissions::add));

            Role consultantRole = Role.builder()
                    .name("CONSULTANT")
                    .permissions(consultantPermissions)
                    .build();
            roleRepository.save(consultantRole);
            log.info(">>> Rôle créé : CONSULTANT avec {} permissions", consultantPermissions.size());
        }

        // ─── Étape 6 : Compte SUPER_ADMIN initial ───────────────────────
        if (userRepository.count() == 0) {
            Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("Rôle SUPER_ADMIN introuvable"));

            User admin = User.builder()
                    .username("admin@monarchive.com")
                    .password(passwordEncoder.encode("Admin@2026!"))
                    .name("Super")
                    .surname("Admin")
                    .phone("+0000000000")
                    .role(superAdminRole)
                    .status(User.UserStatus.ACTIVE)
                    .build();

            userRepository.save(admin);
            log.warn(">>> Compte SUPER_ADMIN créé : admin@monarchive.com / Admin@2026! — À CHANGER !");
        }

        log.info(">>> Initialisation terminée.");
    }
}

