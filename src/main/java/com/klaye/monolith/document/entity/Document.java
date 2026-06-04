package com.klaye.monolith.document.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un Document archivé pour une mission ou un dossier.
 * Migrée depuis Document-Service vers le Monolithe.
 * Utilise un UUID comme identifiant primaire pour la sécurité des fichiers.
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document implements Persistable<UUID> {
 
    @Transient
    private boolean isNew = true;

    @Id
    private UUID id;

    @Column(name = "nom_fichier_original", nullable = false)
    private String nomFichierOriginal;

    @Column(name = "nom_fichier_stocke", nullable = false, unique = true)
    private String nomFichierStocke;

    @Column(name = "chemin_stockage", nullable = false)
    private String cheminStockage;

    @Column(name = "type_fichier", nullable = false)
    private String typeFichier;

    @Column(name = "taille", nullable = false)
    private Long taille;

    @Column(name = "code_dossier")
    private String codeDossier;

    @Column(name = "code_mission")
    private String codeMission;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    protected void setNotNew() {
        this.isNew = false;
    }
}
