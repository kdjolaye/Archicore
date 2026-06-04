Système de Gestion d'Audit et d'Archivage (Monolithe-App)

Ce projet est une application monolithique moderne de gestion d'audit et d'archivage documentaire. Il se compose d'un backend en Spring Boot (Java 21) et d'un frontend en Angular 21 s'appuyant sur PrimeNG et Tailwind CSS.

--

Architecture du Projet

Le dépôt est organisé de la manière suivante :

Racine / Backend : Projet Maven Spring Boot (contenant src/, pom.xml, etc.).

ArchiCore/ : L'application Angular (frontend) contenant le code source de l'interface utilisateur.

docker/ et docker-compose.yml : Fichiers de configuration pour conteneuriser l'application et sa base de données MySQL.

Modèle de Rôles et Permissions

Le système applique un contrôle d'accès strict basé sur les rôles et permissions (DataInitializer.java).

SUPER_ADMIN : Accès global total (bypass de tous les contrôles de permissions). Il est responsable de la création et de la gestion des comptes ADMIN_PRINCIPAL.

ADMIN_PRINCIPAL : Accès global total (bypass des contrôles). Il gère les dossiers clients, les missions d'audit et l'assignation des administrateurs/consultants.

ADMIN : A des privilèges CRUD uniquement sur les missions d'audit auxquelles il a été spécifiquement assigné. Il a un accès en lecture seule sur les autres éléments.

CONSULTANT : Lecture seule stricte sur l'ensemble de l'application (Dossiers, Missions, Documents). Aucun bouton d'écriture ne lui est accessible.

Permissions Métier Clés

Gestion des comptes : USER_READ, USER_UPDATE

Dossiers : DOSSIER_CREATE, DOSSIER_READ, DOSSIER_UPDATE, DOSSIER_DELETE

Missions : MISSION_CREATE, MISSION_READ, MISSION_UPDATE, MISSION_DELETE

Documents : DOCUMENT_CREATE, DOCUMENT_READ, DOCUMENT_DELETE

--

Guide de l'API Backend (Endpoints REST)

Tous les endpoints backend sont préfixés par /api/v1. La sécurité repose sur un cookie HttpOnly sécurisé contenant le JWT (accessToken et refreshToken).

Authentification (/api/v1/auth)

POST /api/v1/auth/login
Description : Authentifie l'utilisateur, dépose les cookies HttpOnly accessToken et refreshToken.
Sécurité / Accès : Public

POST /api/v1/auth/refresh
Description : Utilise le refreshToken pour renouveler le cookie d'accès accessToken.
Sécurité / Accès : Authentifié

POST /api/v1/auth/logout
Description : Invalide le jeton en base et supprime les cookies HttpOnly.
Sécurité / Accès : Authentifié

GET /api/v1/auth/me
Description : Récupère les informations de base de l'utilisateur connecté.
Sécurité / Accès : Authentifié

Gestion des Utilisateurs (/api/v1/users)

GET /api/v1/users
Description : Récupère la liste de tous les utilisateurs du système.
Sécurité / Accès : Permission USER_READ

POST /api/v1/users
Description : Crée un compte utilisateur et génère un mot de passe temporaire à changer.
Sécurité / Accès : Permission USER_WRITE

GET /api/v1/users/me
Description : Récupère le profil complet de l'utilisateur actuellement connecté.
Sécurité / Accès : Authentifié

PUT /api/v1/users/me/profile
Description : Met à jour les informations du profil connecté (Nom, Prénom, Téléphone).
Sécurité / Accès : Authentifié

PATCH /api/v1/users/me/password
Description : Change le mot de passe de l'utilisateur connecté (requis si forcé).
Sécurité / Accès : Authentifié

PUT /api/v1/users/{userName}/complete-profile
Description : Finalise l'activation du compte lors de la première connexion.
Sécurité / Accès : Authentifié

PUT /api/v1/users/{userName}/suspend
Description : Suspend temporairement un compte utilisateur.
Sécurité / Accès : Permission USER_WRITE

PUT /api/v1/users/{userName}/activate
Description : Réactive un compte utilisateur suspendu.
Sécurité / Accès : Permission USER_WRITE

DELETE /api/v1/users/{userName}
Description : Supprime définitivement un compte utilisateur.
Sécurité / Accès : Permission USER_WRITE

Rôles et Permissions (/api/v1/roles & /api/v1/permissions)

GET /api/v1/roles
Description : Récupère la liste complète des rôles et leurs permissions associées.
Sécurité / Accès : Permission Role.READ

POST /api/v1/roles
Description : Crée un nouveau rôle personnalisé avec des permissions par défaut.
Sécurité / Accès : Permission Role.WRITE

PUT /api/v1/roles/{id}/permissions
Description : Modifie ou assigne des permissions à un rôle spécifique.
Sécurité / Accès : Permission Role.WRITE

DELETE /api/v1/roles/{roleName}/permissions
Description : Supprime des permissions ciblées d'un rôle.
Sécurité / Accès : Permission Role.DELETE

DELETE /api/v1/roles/{roleName}/permissions/all
Description : Supprime toutes les permissions affectées à un rôle.
Sécurité / Accès : Permission Role.DELETE

GET /api/v1/permissions
Description : Récupère la liste globale de toutes les permissions système définies.
Sécurité / Accès : Permission Permission.READ

POST /api/v1/permissions
Description : Ajoute une nouvelle permission au dictionnaire global.
Sécurité / Accès : Permission Permission.WRITE

Gestion des Dossiers (/api/v1/dossiers)

GET /api/v1/dossiers
Description : Liste tous les dossiers clients (page d'accueil).
Sécurité / Accès : Public (Lecture publique)

GET /api/v1/dossiers/{codeDossier}
Description : Récupère les détails d'un dossier par son code unique.
Sécurité / Accès : Permission Dossier.READ

POST /api/v1/dossiers
Description : Crée un nouveau dossier client.
Sécurité / Accès : Permission Dossier.WRITE

PUT /api/v1/dossiers/{codeDossier}
Description : Met à jour les informations d'un dossier client.
Sécurité / Accès : Permission Dossier.WRITE

DELETE /api/v1/dossiers/{codeDossier}
Description : Supprime définitivement un dossier.
Sécurité / Accès : Permission Dossier.DELETE

Gestion des Missions (/api/v1/missions)

GET /api/v1/missions
Description : Liste toutes les missions d'audit créées.
Sécurité / Accès : Permission Mission.READ

GET /api/v1/missions/{code}
Description : Récupère les détails d'une mission d'audit (ex: équipe, statut).
Sécurité / Accès : Permission Mission.READ

POST /api/v1/missions
Description : Crée une nouvelle mission d'audit.
Sécurité / Accès : Permission Mission.WRITE

PUT /api/v1/missions/{code}
Description : Met à jour les détails d'une mission d'audit.
Sécurité / Accès : Permission Mission.WRITE

DELETE /api/v1/missions/{code}
Description : Supprime une mission d'audit.
Sécurité / Accès : Permission Mission.DELETE

GET /api/v1/missions/available-for-admin
Description : Liste les missions d'audit n'ayant aucun administrateur assigné.
Sécurité / Accès : Super/Principal Admin

POST /api/v1/missions/{code}/assign
Description : Assigne un administrateur à une mission d'audit.
Sécurité / Accès : Permission Mission.WRITE

DELETE /api/v1/missions/{code}/assign/{userId}
Description : Retire l'assignation d'un administrateur sur une mission d'audit.
Sécurité / Accès : Permission Mission.WRITE

GET /api/v1/missions/{code}/assign
Description : Récupère la liste des administrateurs affectés à une mission.
Sécurité / Accès : Super/Principal Admin

GET /api/v1/missions/{code}/assigned-me
Description : Renvoie true si l'utilisateur actuellement connecté est affecté à la mission.
Sécurité / Accès : Permission Mission.READ

GET /api/v1/missions/my-missions
Description : Liste les codes de toutes les missions assignées à l'utilisateur connecté.
Sécurité / Accès : Permission Mission.READ

GET /api/v1/missions/assigned-to-user/{userId}
Description : Liste les missions affectées à un utilisateur spécifique.
Sécurité / Accès : Super/Principal Admin

POST /api/v1/missions/{code}/equipe
Description : Ajoute un membre (consultant) à l'équipe de la mission.
Sécurité / Accès : Permission Mission.WRITE

GET /api/v1/missions/{code}/equipe
Description : Liste tous les membres d'équipe (consultants) affectés à la mission.
Sécurité / Accès : Permission Mission.READ

Gestion Documentaire (/api/v1/documents)

POST /api/v1/documents/upload
Description : Téléverse un fichier (PDF, Word, Excel, Images) lié à un dossier et une mission.
Sécurité / Accès : Permission Document.WRITE

GET /api/v1/documents
Description : Récupère la liste des documents associés à une mission et un dossier spécifiques.
Sécurité / Accès : Permission Document.READ

GET /api/v1/documents/download/{id}
Description : Télécharge le fichier physique d'un document par son UUID.
Sécurité / Accès : Permission Document.READ

DELETE /api/v1/documents/{id}
Description : Supprime logiquement et physiquement un document.
Sécurité / Accès : Permission Document.DELETE

Journalisation d'Audit (/api/v1/audit)

GET /api/v1/audit/tous
Description : Historique global de toutes les actions (création, suppression, etc.).
Sécurité / Accès : Permission AuditLog.READ

GET /api/v1/audit/ressource
Description : Filtre les logs pour une ressource précise (ex: type=Document, id=UUID).
Sécurité / Accès : Permission AuditLog.READ

GET /api/v1/audit/utilisateur
Description : Filtre les logs des actions menées par un utilisateur spécifique.
Sécurité / Accès : Permission AuditLog.READ

GET /api/v1/audit/action
Description : Filtre les logs par type d'action (ex: CONNEXION, SUPPRIMER_DOCUMENT).
Sécurité / Accès : Permission AuditLog.READ

GET /api/v1/audit/periode
Description : Filtre les logs exécutés entre deux dates précises.
Sécurité / Accès : Permission AuditLog.READ

Architecture de l'Interface Utilisateur (Angular)

Le frontend ArchiCore est bâti sur l'architecture Angular Standalone. Il offre une interface moderne, intuitive et responsive développée avec PrimeNG 21 et Tailwind CSS.

Structure des Écrans

Dossiers Clients (/dossiers) : Liste des dossiers ouverts au public (consultation publique).

Détails Dossier (/dossiers/:codeDossier/detail) : Affiche les informations du dossier et la liste de ses missions d'audit.

Détails Mission (/dossiers/:codeDossier/missions/:codeMission/detail) :

Informations générales de la mission (statut, dates, etc.).

Section Documents : Module de téléversement sécurisé de fichiers et table de téléchargement/suppression.

Section Équipe (Ajoutée récemment) : Liste des consultants participant à la mission, avec possibilité d'ajouter des membres via un modal interactif pour les profils habilités (Super Admin, Principal Admin, et Admin assigné à la mission).

Administration des Comptes (/users) : Liste globale des utilisateurs, création interactive de comptes, activation, suspension et suppression d'utilisateurs.

Administration Rôles & Permissions (/roles-permissions) : Création de nouveaux rôles métier et modification dynamique des permissions via des cases à cocher PrimeNG.

Journal d'Audit (/audit) : Interface avancée à onglets permettant aux auditeurs de suivre tous les logs du système avec des filtres combinés.

Mon Profil (/profile & /change-password) : Permet à l'utilisateur de modifier ses données personnelles ou de changer son mot de passe avec validation de formulaire dynamique (le bouton Enregistrer s'active uniquement si des modifications réelles sont saisies).

Instructions pour Démarrer le Projet (Windows)

Prérequis

Assurez-vous d'avoir installé les outils suivants sur votre poste Windows :

Java JDK 21 (ex: Eclipse Temurin 21)

Node.js (version 18 ou supérieure recommandée)

MySQL (exécuté localement sur le port par défaut 3306) OU Docker Desktop

Étape 1 : Préparation de la Base de Données

Démarrez votre serveur MySQL.

Créez un schéma nommé audit_monolith_db (si non créé automatiquement) :
CREATE DATABASE IF NOT EXISTS audit_monolith_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

Par défaut, l'application utilise l'utilisateur root avec le mot de passe root sur localhost:3306. Si vos identifiants MySQL diffèrent, vous pouvez :

Soit éditer le fichier application.yml (lignes 11-13).

Soit configurer des variables d'environnement dans votre terminal PowerShell :
$env:SPRING_DATASOURCE_USERNAME="votre_utilisateur"
$env:SPRING_DATASOURCE_PASSWORD="votre_mot_de_passe"

Étape 2 : Démarrage du Backend (Spring Boot)

Option A : Démarrage Local (Recommandé pour le développement)

Ouvrez un terminal Windows (PowerShell ou Command Prompt) à la racine du projet (c:\Users\DELL\Desktop\Monolithe-App) :

Construisez et démarrez l'application avec le Maven Wrapper fourni :
.\mvnw.cmd spring-boot:run

L'API démarrera sur le port 8080.

Vous pouvez accéder à la documentation interactive OpenAPI Swagger à l'adresse suivante :
http://localhost:8080/swagger-ui/index.html

Option B : Démarrage via Docker Compose

Si vous préférez utiliser Docker pour lancer le backend et MySQL dans des conteneurs :

Copiez le fichier d'exemple pour créer votre fichier .env :
copy .env.example .env

Lancez le build et démarrez les conteneurs :
docker compose up --build -d

Cela démarrera le conteneur MySQL sur le port d'hôte défini dans .env et l'application sur le port 8080.

Étape 3 : Démarrage du Frontend (Angular)

Ouvrez un deuxième terminal Windows dans le dossier du frontend (c:\Users\DELL\Desktop\Monolithe-App\ArchiCore) :

Entrez dans le dossier frontend :
cd ArchiCore

Installez toutes les dépendances NPM requises :
npm install

Lancez le serveur de développement Angular :
npm run start

Une fois compilé, ouvrez votre navigateur et accédez à l'adresse :
http://localhost:4200

Étape Optionnelle : Régénérer le Client API

Si vous effectuez des modifications sur les contrôleurs backend et souhaitez synchroniser les services Angular :

Assurez-vous que l'OpenAPI spec ArchiveBackend.json est à jour.

Dans le dossier ArchiCore, lancez la commande :
npm run api:generate

Identifiants de Connexion par Défaut

Au tout premier démarrage, le système s'auto-initialise en créant le rôle SUPER_ADMIN et un premier utilisateur administrateur.

Nom d'utilisateur / Email : admin@monarchive.com

Mot de passe : Admin@2026!