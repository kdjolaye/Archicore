package com.klaye.monolith.dossier.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entité représentant un Dossier client (entité auditée).
 * Migrée depuis Dossier-Service vers le Monolithe.
 */
@Entity
@Table(name = "dossiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codeDossier; // Code unique identifiant le dossier

    @Column(nullable = false)
    private String raisonSociale; // Nom de l'entreprise/entité

    private String typeEntreprise;
    private String adresse;
    private String phone;
    private String email;
    private String bp; // Boîte Postale

    // Audit — ID de l'utilisateur ayant créé/modifié le dossier
    private Long createdBy;
    private Long updatedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
