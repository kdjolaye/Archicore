package com.klaye.monolith.mission.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant l'appartenance d'un utilisateur à une équipe de mission.
 * Migrée depuis Mission-Service vers le Monolithe.
 */
@Entity
@Table(name = "equipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codeMission; // Lien avec la mission via son code

    @Column(nullable = false)
    private Long userId; // Référence vers l'utilisateur (entité User dans le module user)

    @Enumerated(EnumType.STRING)
    private Poste poste;

    /**
     * Rôles/Postes possibles au sein d'une équipe de mission.
     */
    public enum Poste {
        CHEF_MISSION, SUPERVISEUR, SENIOR, JUNIOR, ASSISTANT, STAGIAIRE
    }
}
