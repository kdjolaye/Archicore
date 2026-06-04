package com.klaye.monolith.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entité représentant une Mission d'audit.
 * Migrée depuis Mission-Service vers le Monolithe.
 */
@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codeMission; // Identifiant unique de la mission

    private String exercice; // Année d'exercice de la mission

    @Enumerated(EnumType.STRING)
    private StatutMission statut;

    private String codeDossier; // Référence vers le dossier associé

    // Métadonnées de traçabilité
    private Long createdBy;
    private Long updatedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * États possibles pour une mission.
     */
    public enum StatutMission {
        PLANIFIEE, EN_COURS, TERMINEE, SUSPENDUE
    }
}
