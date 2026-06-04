package com.klaye.monolith.user.controller;

import com.klaye.monolith.user.dto.PermissionRequest;
import com.klaye.monolith.user.entity.Permission;
import com.klaye.monolith.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des Permissions.
 * Migré depuis User-Service vers le Monolithe.
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'Permission', 'READ')")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'Permission', 'WRITE')")
    public ResponseEntity<Permission> createPermission(@RequestBody PermissionRequest request) {
        return new ResponseEntity<>(
                permissionService.createPermission(request.name()),
                HttpStatus.CREATED);
    }
}
