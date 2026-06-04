package com.klaye.monolith.user.repository;

import com.klaye.monolith.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des Utilisateurs.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Recherche un utilisateur par son nom d'utilisateur (email).
     */
    Optional<User> findByUsername(String username);

    /**
     * Vérifie si un nom d'utilisateur existe déjà.
     */
    boolean existsByUsername(String username);
}
