package com.klaye.monolith.dossier.repository;

import com.klaye.monolith.dossier.entity.Dossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des Dossiers clients.
 * Migré depuis Dossier-Service vers le Monolithe.
 */
@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long> {
    
    /**
     * Recherche un dossier par son code unique.
     */
    Optional<Dossier> findByCodeDossier(String codeDossier);

    /**
     * Vérifie si un dossier existe déjà avec ce code.
     */
    boolean existsByCodeDossier(String codeDossier);
}
