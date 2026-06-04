package com.klaye.monolith.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Table de liaison entre un Utilisateur (rôle ADMIN) et une Mission.
 * Permet de contrôler les droits d'écriture/suppression d'un ADMIN
 * uniquement sur les missions qui lui sont explicitement assignées.
 *
 * L'assignation est faite par un SUPER_ADMIN ou ADMIN_PRINCIPAL.
 */
@Entity
@Table(name = "user_missions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "mission_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mission_id", nullable = false)
    private Long missionId;

    /**
     * Code de la mission (dénormalisé pour performance).
     * Évite un JOIN avec la table missions lors de la vérification
     * dans le PermissionEvaluator.
     */
    @Column(name = "code_mission", nullable = false)
    private String codeMission;

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;

    /**
     * ID de l'utilisateur ayant effectué l'assignation
     * (SUPER_ADMIN ou ADMIN_PRINCIPAL).
     */
    @Column(name = "assigned_by")
    private Long assignedBy;
}
