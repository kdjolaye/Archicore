package com.klaye.monolith.user.controller;

import com.klaye.monolith.user.dto.RoleRequest;
import com.klaye.monolith.user.entity.Role;
import com.klaye.monolith.user.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Contrôleur pour la gestion des Rôles.
 * Migré depuis User-Service vers le Monolithe.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'Role', 'READ')")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'Role', 'WRITE')")
    public ResponseEntity<Role> createRole(@Valid @RequestBody RoleRequest request) {
        return new ResponseEntity<>(
                roleService.createRole(request.name(), request.permissionNames()),
                HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}/permissions",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'Role', 'WRITE')")
    public ResponseEntity<Role> updatePermissions(@PathVariable Long id, @RequestBody Set<String> permissionNames) {
        return ResponseEntity.ok(roleService.updatePermissions(id, permissionNames));
    }

    @DeleteMapping("/{roleName}/permissions")
    @PreAuthorize("hasPermission(null, 'Role', 'DELETE')")
    public ResponseEntity<Void> removePermissions(@PathVariable String roleName, @RequestBody Set<String> permissionNames) {
        roleService.removePermissions(roleName, permissionNames);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{roleName}/permissions/all",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'Role', 'DELETE')")
    public ResponseEntity<Void> removeAllPermissions(@PathVariable String roleName) {
        roleService.removeAllPermissions(roleName);
        return ResponseEntity.noContent().build();
    }
}
