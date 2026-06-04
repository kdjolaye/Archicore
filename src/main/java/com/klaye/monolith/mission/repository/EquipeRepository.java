package com.klaye.monolith.mission.repository;

import com.klaye.monolith.mission.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion des Équipes de mission.
 * Migré depuis Mission-Service vers le Monolithe.
 */
@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    
    /**
     * Récupère tous les membres d'une équipe pour une mission donnée.
     */
    List<Equipe> findByCodeMission(String codeMission);

    /**
     * Vérifie si un utilisateur est déjà membre d'une équipe pour une mission spécifique.
     */
    boolean existsByCodeMissionAndUserId(String codeMission, Long userId);
}
