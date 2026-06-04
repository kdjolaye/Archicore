package com.klaye.monolith.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse d'authentification contenant le jeton JWT.
 */
@Data
@NoArgsConstructor
public class AuthResponse {
    private Long id;
    private String email;
    private String role;
    private String status;
    private String message;

    public AuthResponse(Long id, String email, String role, String status, String message) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.status = status;
        this.message = message;
    }

    public AuthResponse(String message) {
        this.message = message;
    }
}
