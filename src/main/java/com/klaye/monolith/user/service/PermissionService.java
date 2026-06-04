package com.klaye.monolith.user.service;

import com.klaye.monolith.user.entity.Permission;
import com.klaye.monolith.common.exception.ResourceAlreadyExistsException;
import com.klaye.monolith.user.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service gérant la logique métier des Permissions.
 * Migré depuis User-Service vers le Monolithe.
 */
@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final PermissionRepository permissionRepository;

    /**
     * Liste toutes les permissions existantes.
     */
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    /**
     * Crée une nouvelle permission.
     */
    public Permission createPermission(String name) {
        if (permissionRepository.findByName(name).isPresent()) {
            throw new ResourceAlreadyExistsException("La permission '" + name + "' existe déjà.");
        }
        return permissionRepository.save(Permission.builder().name(name).build());
    }
}
