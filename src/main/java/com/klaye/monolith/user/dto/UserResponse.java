package com.klaye.monolith.user.dto;

import java.time.LocalDateTime;

/**
 * Réponse contenant les informations publiques d'un utilisateur.
 */
public record UserResponse(
        Long id,
        String username,
        String name,
        String surname,
        String phone,
        String status,
        LocalDateTime createdAt,
        RoleResponse role
) {}
