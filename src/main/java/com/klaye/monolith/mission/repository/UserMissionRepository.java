package com.klaye.monolith.mission.repository;

import com.klaye.monolith.mission.entity.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion des assignations Mission → Utilisateur (ADMIN).
 * Utilisé principalement par le CustomPermissionEvaluator pour vérifier
 * si un ADMIN a le droit d'écrire/supprimer sur une mission donnée.
 */
@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {

    /**
     * Vérifie si un utilisateur est assigné à une mission (par codeMission).
     * Requête centrale du PermissionEvaluator.
     */
    boolean existsByUserIdAndCodeMission(Long userId, String codeMission);

    /**
     * Récupère toutes les missions assignées à un utilisateur.
     */
    List<UserMission> findByUserId(Long userId);

    /**
     * Récupère tous les utilisateurs assignés à une mission.
     */
    List<UserMission> findByCodeMission(String codeMission);

    /**
     * Vérifie si une assignation existe déjà (pour éviter les doublons).
     */
    boolean existsByUserIdAndMissionId(Long userId, Long missionId);

    /**
     * Supprime toutes les assignations d'un utilisateur.
     */
    void deleteByUserId(Long userId);

    void deleteByUserIdAndCodeMission(Long userId, String codeMission);
}
