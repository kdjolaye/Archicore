package com.klaye.monolith.user.repository;

import com.klaye.monolith.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des Permissions.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * Recherche une permission par son nom.
     */
    Optional<Permission> findByName(String name);
}
