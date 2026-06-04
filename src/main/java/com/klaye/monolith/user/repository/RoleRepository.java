package com.klaye.monolith.user.repository;

import com.klaye.monolith.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des Rôles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Recherche un rôle par son nom.
     */
    Optional<Role> findByName(String name);
}
