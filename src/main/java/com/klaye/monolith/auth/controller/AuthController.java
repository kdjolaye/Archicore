package com.klaye.monolith.auth.controller;

import com.klaye.monolith.auth.dto.AuthRequest;
import com.klaye.monolith.auth.dto.AuthResponse;
import com.klaye.monolith.auth.dto.TokenResult;
import com.klaye.monolith.auth.service.AuthService;
import com.klaye.monolith.auth.service.JwtService;
import com.klaye.monolith.auth.service.RefreshTokenService;
import com.klaye.monolith.auth.config.JwtUserDetails;
import com.klaye.monolith.auth.entity.RefreshToken;
import com.klaye.monolith.common.exception.TokenRefreshException;
import com.klaye.monolith.user.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Arrays;

/**
 * Contrôleur pour l'authentification dans le Monolithe.
 * Ce contrôleur expose les endpoints nécessaires pour se connecter.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    // Durée du cookie (ex: 1 heure pour l'accessToken, 7 jours pour le refreshToken)
    private static final int ACCESS_TOKEN_EXPIRATION = 3600; 
    private static final int REFRESH_TOKEN_EXPIRATION = 604800;

    /**
     * Endpoint de connexion.
     * Valide les identifiants et retourne un jeton JWT.
     * 
     * @param authRequest Contient username et password
     * @return AuthResponse avec le token
     */
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        try {
            log.info("Tentative de connexion pour {}", authRequest.getUsername());
            TokenResult tokenResult = authService.authenticate(authRequest);
            User user = tokenResult.getUser();

            ResponseCookie jwtCookie = ResponseCookie.from("accessToken", tokenResult.getAccessToken())
                    .httpOnly(true)
                    .secure(false) // Mettre à true en production (HTTPS)
                    .path("/")
                    .maxAge(ACCESS_TOKEN_EXPIRATION)
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenResult.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // Mettre à true en production
                    .path("/")
                    .maxAge(REFRESH_TOKEN_EXPIRATION)
                    .sameSite("Lax")
                    .build();

            AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().getName(),
                    user.getStatus() != null ? user.getStatus().name() : null,
                    "Connexion réussie"
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(new AuthResponse("Echec : " + e.getMessage()));
        }
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
        String refreshToken = getCookieValue(request, "refreshToken");
        
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(new AuthResponse("Refresh token manquant."));
        }

        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String roleName = user.getRole().getName();
                    List<String> permissions = user.getRole().getPermissions().stream()
                            .map(p -> p.getName())
                            .collect(Collectors.toList());

                    String newAccessToken = jwtService.generateToken(user.getUsername(), roleName, permissions, user.getId());

                    ResponseCookie jwtCookie = ResponseCookie.from("accessToken", newAccessToken)
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .maxAge(ACCESS_TOKEN_EXPIRATION)
                            .sameSite("Lax")
                            .build();

                    AuthResponse response = new AuthResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getRole().getName(),
                            user.getStatus() != null ? user.getStatus().name() : null,
                            "Token rafraîchi avec succès"
                    );

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                            .body(response);
                })
                .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token introuvable en base."));
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
        String refreshToken = getCookieValue(request, "refreshToken");
        
        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken).ifPresent(token -> {
                refreshTokenService.deleteByUserId(token.getUser().getId());
            });
        }

        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new AuthResponse("Déconnexion réussie"));
    }

    @GetMapping(value = "/me",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) principal;
            User user = authService.getUserByUsername(userDetails.email());
            AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().getName(),
                    user.getStatus() != null ? user.getStatus().name() : null,
                    "Profil récupéré avec succès"
            );
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body(new AuthResponse("Non authentifié"));
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> name.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
