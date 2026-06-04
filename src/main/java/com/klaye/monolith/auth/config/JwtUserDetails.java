package com.klaye.monolith.auth.config;

/**
 * Objet léger stocké comme 'principal' dans le SecurityContext.
 * Contient l'email et l'ID de l'utilisateur extraits du JWT.
 * Le toString() retourne l'email pour que authentication.getName() reste compatible.
 */
public record JwtUserDetails(String email, Long userId) {

    @Override
    public String toString() {
        return email;
    }
}
