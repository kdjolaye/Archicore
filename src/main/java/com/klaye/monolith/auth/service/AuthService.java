package com.klaye.monolith.auth.service;

import com.klaye.monolith.auth.dto.AuthRequest;
import com.klaye.monolith.user.entity.Permission;
import com.klaye.monolith.user.entity.User;
import com.klaye.monolith.user.repository.UserRepository;
import com.klaye.monolith.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.klaye.monolith.auth.entity.RefreshToken;
import com.klaye.monolith.auth.dto.TokenResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service métier pour l'authentification dans le Monolithe.
 * Remplace l'ancien Auth-Service en utilisant directement le UserRepository.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Authentifie un utilisateur et génère un jeton JWT.
     *
     * @param request Requête contenant le username et le password
     * @return Le TokenResult contenant l'utilisateur et les jetons
     */
    public TokenResult authenticate(AuthRequest request) {
        // 1. Recherche de l'utilisateur
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Identifiants incorrects"));

        // 2. Vérification du statut du compte
        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new DisabledException("Ce compte a été suspendu.");
        }

        // 3. Vérification du mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Identifiants incorrects");
        }

        // 4. Vérification de l'expiration du mot de passe (30 jours) pour les comptes ACTIVE
        if (user.getStatus() == User.UserStatus.ACTIVE) {
            boolean neverChanged = user.getPasswordChangedAt() == null;
            boolean expired = !neverChanged &&
                    user.getPasswordChangedAt().isBefore(LocalDateTime.now().minusDays(30));
            if (neverChanged || expired) {
                user.setStatus(User.UserStatus.PASSWORD_EXPIRED);
                userRepository.save(user);
            }
        }

        // 4. Extraction des rôle et permissions pour le JWT
        String roleName = user.getRole().getName();
        List<String> permissions = user.getRole().getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());

        // 5. Génération du JWT avec rôle, permissions et ID utilisateur
        String accessToken = jwtService.generateToken(user.getUsername(), roleName, permissions, user.getId());

        // 6. Génération ou mise à jour du Refresh Token
        // On supprime d'abord les anciens refresh tokens de l'utilisateur
        refreshTokenService.deleteByUserId(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new TokenResult(user, accessToken, refreshToken.getToken());
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur (email)
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + username));
    }
}
