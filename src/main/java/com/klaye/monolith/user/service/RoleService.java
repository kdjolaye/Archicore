package com.klaye.monolith.user.service;

import com.klaye.monolith.user.entity.Permission;
import com.klaye.monolith.user.entity.Role;
import com.klaye.monolith.common.exception.ResourceAlreadyExistsException;
import com.klaye.monolith.common.exception.ResourceNotFoundException;
import com.klaye.monolith.user.repository.PermissionRepository;
import com.klaye.monolith.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service gérant la logique métier des Rôles et des Permissions.
 * Migré depuis User-Service vers le Monolithe.
 */
@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé avec l'id: " + id));
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé avec le nom: " + name));
    }

    /**
     * Crée un nouveau rôle avec un ensemble de permissions.
     */
    @Transactional
    public Role createRole(String name, Set<String> permissionNames) {
        if (roleRepository.findByName(name).isPresent()) {
            throw new ResourceAlreadyExistsException("Le rôle existe déjà: " + name);
        }

        Set<Permission> permissions = permissionNames.stream()
                .map(permName -> permissionRepository.findByName(permName)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée: " + permName)))
                .collect(Collectors.toSet());

        Role role = Role.builder()
                .name(name)
                .permissions(permissions)
                .build();

        return roleRepository.save(role);
    }

    /**
     * Met à jour les permissions d'un rôle existant (remplace toutes les permissions).
     */
    @Transactional
    public Role updatePermissions(Long roleId, Set<String> permissionNames) {
        Role role = findById(roleId);

        Set<Permission> permissions = permissionNames.stream()
                .map(permName -> permissionRepository.findByName(permName)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée: " + permName)))
                .collect(Collectors.toSet());

        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    /**
     * Retire des permissions spécifiques d'un rôle sans toucher aux autres.
     */
    @Transactional
    public void removePermissions(String roleName, Set<String> permissionNames) {
        Role role = findByName(roleName);

        Set<Permission> toRemove = permissionNames.stream()
                .map(permName -> permissionRepository.findByName(permName)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée: " + permName)))
                .collect(Collectors.toSet());

        role.getPermissions().removeAll(toRemove);
        roleRepository.save(role);
    }

    /**
     * Retire toutes les permissions d'un rôle.
     */
    @Transactional
    public void removeAllPermissions(String roleName) {
        Role role = findByName(roleName);
        role.getPermissions().clear();
        roleRepository.save(role);
    }
}
