package com.klaye.monolith.auth.config;

import com.klaye.monolith.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtre de sécurité interceptant chaque requête pour valider le token JWT.
 * Migré depuis les microservices pour centraliser la validation dans le monolithe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.extractAllClaims(jwt);
            String userEmail = claims.getSubject();
            String role = claims.get("role", String.class);
            List<String> permissions = claims.get("permissions", List.class);

            // Si l'utilisateur n'est pas encore authentifié dans le contexte Spring Security
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<GrantedAuthority> authorities = new ArrayList<>();

                // Ajout du rôle avec le préfixe ROLE_ attendu par Spring Security
                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }

                // Ajout des permissions individuelles
                if (permissions != null) {
                    for (String perm : permissions) {
                        authorities.add(new SimpleGrantedAuthority(perm));
                    }
                }

                // Extraction du userId pour le PermissionEvaluator
                Long userId = claims.get("userId", Long.class);
                JwtUserDetails principal = new JwtUserDetails(userEmail, userId);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // On met à jour le contexte avec l'utilisateur authentifié
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Utilisateur {} authentifié avec succès", userEmail);
            }

        } catch (Exception e) {
            log.error(">>> Erreur de validation JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
