package com.klaye.monolith.audit.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Utilitaire partagé pour récupérer les informations
 * de l'utilisateur connecté et de sa requête HTTP.
 */
public final class AuditUtils {

    // Classe utilitaire — pas d'instanciation
    private AuditUtils() {}

    /**
     * Retourne le username de l'utilisateur connecté.
     * Retourne "anonymous" si aucun utilisateur n'est authentifié.
     */
    public static String getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .orElse("anonymous");
    }

    /**
     * Retourne l'IP réelle du client.
     * Gère les proxies via l'en-tête X-Forwarded-For.
     * Retourne "unknown" si la requête n'est pas accessible.
     */
    public static String getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes()).getRequest();

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();

        } catch (Exception e) {
            return "unknown";
        }
    }
}