package com.klaye.monolith.dossier.repository;

import com.klaye.monolith.dossier.entity.DossierUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion de l'affectation des utilisateurs aux Dossiers.
 * Migré depuis Dossier-Service vers le Monolithe.
 */
@Repository
public interface DossierUserRepository extends JpaRepository<DossierUser, Long> {
    
    /**
     * Récupère tous les utilisateurs affectés à un dossier.
     */
    List<DossierUser> findByDossierId(Long dossierId);

    /**
     * Récupère tous les dossiers auxquels un utilisateur est affecté.
     */
    List<DossierUser> findByUserId(Long userId);
}
