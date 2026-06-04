package com.klaye.monolith.mission.repository;

import com.klaye.monolith.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des Missions.
 * Migré depuis Mission-Service vers le Monolithe.
 */
@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    
    /**
     * Recherche une mission par son code unique.
     */
    Optional<Mission> findByCodeMission(String codeMission);

    /**
     * Vérifie si une mission existe déjà avec ce code.
     */
    boolean existsByCodeMission(String codeMission);
}
