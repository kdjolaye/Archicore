package com.klaye.monolith.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entité représentant un Utilisateur dans le système.
 * Cette classe a été migrée depuis User-Service vers le Monolithe.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Email utilisé comme identifiant

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // Association avec le Rôle (chargement immédiat pour la sécurité)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    /**
     * Statuts possibles pour un utilisateur.
     */
    public enum UserStatus {
        FORCE_PASSWORD_CHANGE, ACTIVE, SUSPENDED, PASSWORD_EXPIRED
    }
}
