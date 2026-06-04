package com.klaye.monolith.user.service;

import com.klaye.monolith.user.dto.AdminUserCreateRequest;
import com.klaye.monolith.user.dto.ChangePasswordRequest;
import com.klaye.monolith.user.dto.UpdateProfileRequest;
import com.klaye.monolith.user.dto.UserProfileCompleteRequest;
import com.klaye.monolith.user.entity.Role;
import com.klaye.monolith.user.entity.User;
import com.klaye.monolith.user.repository.UserRepository;
import com.klaye.monolith.mission.repository.UserMissionRepository;
import com.klaye.monolith.common.exception.ResourceAlreadyExistsException;
import com.klaye.monolith.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service gérant la logique métier des Utilisateurs.
 * Migré depuis User-Service vers le Monolithe.
 * Gère la création par l'admin et la complétion automatique du profil.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMissionRepository userMissionRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le nom : " + username));
    }

    /**
     * Permet à un administrateur de créer un compte utilisateur "pré-activé".
     * Génère un mot de passe temporaire.
     */
    @Transactional
    public String createUserByAdmin(AdminUserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceAlreadyExistsException("Ce nom d'utilisateur (email) existe déjà.");
        }

        Role role = roleService.findByName(request.roleName());

        // Génération d'un mot de passe temporaire de 8 caractères
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(tempPassword))
                .role(role)
                .status(User.UserStatus.FORCE_PASSWORD_CHANGE)
                .name("EN_ATTENTE")
                .surname("EN_ATTENTE")
                .build();

        userRepository.save(user);
        return tempPassword;
    }

    /**
     * Permet à l'utilisateur de compléter son profil lors de sa première connexion obligatoire.
     * Note : pour le changement de mot de passe seul, utiliser changePassword().
     */
    @Transactional
    public void completeProfile(String userName, UserProfileCompleteRequest request) {
        User user = findByUsername(userName);

        user.setName(request.name());
        user.setSurname(request.surname());
        user.setPhone(request.phone());

        // On ne change le mot de passe et le statut que si un nouveau mot de passe est fourni
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setStatus(User.UserStatus.ACTIVE);
        }

        userRepository.save(user);
    }

    /**
     * Permet à un utilisateur de changer son mot de passe (auto-service).
     * Couvre deux cas :
     *   - FORCE_PASSWORD_CHANGE : première connexion avec mot de passe temporaire
     *   - PASSWORD_EXPIRED      : expiration mensuelle du mot de passe
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findByUsername(username);

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
    }

    /**
     * Permet à un utilisateur de mettre à jour ses informations de profil (sans mot de passe).
     */
    @Transactional
    public void updateProfile(String username, UpdateProfileRequest request) {
        User user = findByUsername(username);

        user.setName(request.name());
        user.setSurname(request.surname());
        user.setPhone(request.phone());
        userRepository.save(user);
    }

    @Transactional
    public void suspendUser(String userName) {
        User user = findByUsername(userName);
        user.setStatus(User.UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(String userName) {
        User user = findByUsername(userName);
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userName) {
        User user = findByUsername(userName);
        // Supprimer d'abord les assignations aux missions
        userMissionRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }
}
