package com.klaye.monolith.user.controller;

import com.klaye.monolith.user.dto.AdminUserCreateRequest;
import com.klaye.monolith.user.dto.ChangePasswordRequest;
import com.klaye.monolith.user.dto.UpdateProfileRequest;
import com.klaye.monolith.user.dto.UserProfileCompleteRequest;
import com.klaye.monolith.user.dto.UserResponse;
import com.klaye.monolith.user.mapper.UserMapper;
import com.klaye.monolith.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des Utilisateurs.
 * Migré depuis User-Service vers le Monolithe.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Création d'un utilisateur par un administrateur.
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'User', 'WRITE')")
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        String tempPassword = userService.createUserByAdmin(request);
        return new ResponseEntity<>(Map.of(
                "message", "Utilisateur créé avec succès",
                "temporaryPassword", tempPassword
        ), HttpStatus.CREATED);
    }

    /**
     * Liste tous les utilisateurs.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(null, 'User', 'READ')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    /**
     * Récupère le profil de l'utilisateur connecté.
     */
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userMapper.toResponse(userService.findByUsername(username)));
    }

    /**
     * Finalisation du profil par l'utilisateur.
     */
    @PutMapping(value = "/{userName}/complete-profile",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> completeProfile(
            @PathVariable String userName,
            @Valid @RequestBody UserProfileCompleteRequest request) {
        userService.completeProfile(userName, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Changement de mot de passe auto-service.
     * Accessible à tout utilisateur authentifié, y compris FORCE_PASSWORD_CHANGE et PASSWORD_EXPIRED.
     */
    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changePassword(username, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mise à jour du profil (nom, prénom, téléphone) sans mot de passe.
     */
    @PutMapping(value = "/me/profile",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updateProfile(username, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Suspendre un compte utilisateur.
     */
    @PutMapping("/{userName}/suspend")
    @PreAuthorize("hasPermission(null, 'User', 'WRITE')")
    public ResponseEntity<Void> suspendUser(@PathVariable String userName) {
        userService.suspendUser(userName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activer un compte utilisateur.
     */
    @PutMapping("/{userName}/activate")
    @PreAuthorize("hasPermission(null, 'User', 'WRITE')")
    public ResponseEntity<Void> activateUser(@PathVariable String userName) {
        userService.activateUser(userName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Supprimer un compte utilisateur.
     */
    @DeleteMapping("/{userName}")
    @PreAuthorize("hasPermission(null, 'User', 'WRITE')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userName) {
        userService.deleteUser(userName);
        return ResponseEntity.noContent().build();
    }
}
