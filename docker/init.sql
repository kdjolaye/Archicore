-- =============================================================
-- Script d'initialisation de la base de données audit_monolith_db
-- Exécuté automatiquement par MySQL au PREMIER démarrage du conteneur
-- =============================================================

-- S'assurer que la base existe et est sélectionnée
CREATE DATABASE IF NOT EXISTS audit_monolith_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE audit_monolith_db;

-- =============================================================
-- Permissions de l'utilisateur applicatif
-- =============================================================
GRANT ALL PRIVILEGES ON audit_monolith_db.* TO 'klaye'@'%';
FLUSH PRIVILEGES;

-- =============================================================
-- DONNÉES INITIALES (Bootstrap)
-- Insérées uniquement si les tables sont vides (safe to re-run)
-- =============================================================

-- Note: Les tables sont créées automatiquement par Hibernate (ddl-auto: update)
-- Ce script ajoute uniquement les données de référence obligatoires.

-- Délai pour s'assurer que Hibernate a créé les tables
-- (ce script est exécuté avant Spring Boot, donc les tables n'existent pas encore)
-- Les données de bootstrap sont à insérer via un DataInitializer Spring au démarrage.

-- Pour la documentation : voici les permissions attendues dans l'application
-- Elles seront créées par le DataInitializer Java au démarrage de l'application.

/*
PERMISSIONS MÉTIER PRÉVUES:
  - USER_READ, USER_UPDATE
  - DOSSIER_CREATE, DOSSIER_READ, DOSSIER_UPDATE, DOSSIER_DELETE
  - MISSION_CREATE, MISSION_READ, MISSION_UPDATE, MISSION_DELETE
  - DOCUMENT_CREATE, DOCUMENT_READ, DOCUMENT_DELETE
  - PERMISSIONS_MANAGE

RÔLES PRÉVUS:
  - ADMIN_PRINCIPAL : toutes les permissions
  - CHEF_MISSION    : DOSSIER_READ, MISSION_CREATE, MISSION_READ, MISSION_UPDATE, DOCUMENT_CREATE, DOCUMENT_READ
  - AUDITEUR        : DOSSIER_READ, MISSION_READ, DOCUMENT_READ, DOCUMENT_CREATE
*/
