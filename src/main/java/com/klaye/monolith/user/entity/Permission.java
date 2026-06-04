package com.klaye.monolith.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant une Permission spécifique dans le système.
 * Les permissions sont liées aux Rôles via une relation Many-to-Many.
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Liste des rôles possédant cette permission.
     * La relation est gérée du côté de Role.
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();
}
