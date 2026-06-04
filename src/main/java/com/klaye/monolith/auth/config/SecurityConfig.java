package com.klaye.monolith.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuration globale de la sécurité pour le Monolithe.
 * Centralise les règles d'accès et intègre le PermissionEvaluator personnalisé
 * pour le contrôle d'accès granulaire à 4 rôles.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomPermissionEvaluator customPermissionEvaluator;

    /**
     * Définit l'encodeur de mot de passe (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure le MethodSecurityExpressionHandler avec notre PermissionEvaluator.
     * Permet l'utilisation de hasPermission() dans les annotations @PreAuthorize.
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(customPermissionEvaluator);
        return handler;
    }

    /**
     * Configure la chaîne de filtres de sécurité.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Désactivation du CSRF (API Stateless)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/swagger-ui/index.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/dossiers"
                        ).permitAll()
                        // Endpoints auto-service : accessibles à tout utilisateur authentifié
                        // (y compris FORCE_PASSWORD_CHANGE et PASSWORD_EXPIRED)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/me/password").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/me/profile").authenticated()
                        // On restreint l'accès aux Actuators au SUPER_ADMIN et ADMIN_PRINCIPAL
                        .requestMatchers("/actuator/**").hasAnyRole("SUPER_ADMIN", "ADMIN_PRINCIPAL")
                        // Tout le reste nécessite désormais une authentification
                        .anyRequest().authenticated()
                )
                // Gestion de session Stateless (pas de session côté serveur)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Ajout de notre filtre JWT personnalisé avant le filtre standard d'authentification
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // À configurer via properties en prod
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true); // Indispensable pour les cookies
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

