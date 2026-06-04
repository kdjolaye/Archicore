package com.klaye.monolith.dossier.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité de liaison entre un Dossier et un Utilisateur.
 * Migrée depuis Dossier-Service vers le Monolithe.
 */
@Entity
@Table(name = "dossier_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Référence à l'ID du dossier (liaison logique)
    @Column(nullable = false)
    private Long dossierId;

    // Référence à l'ID de l'utilisateur (liaison logique vers le module User)
    @Column(nullable = false)
    private Long userId;

    // Rôle de l'utilisateur sur ce dossier (ex : CONSULTANT, MANAGER, RESPONSABLE)
    private String role;
}
