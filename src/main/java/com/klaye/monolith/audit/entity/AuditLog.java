package com.klaye.monolith.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(nullable = false)
    private String performedBy;   // email de l'utilisateur

    private Long userId;

    @Column(nullable = false)
    private Instant performedAt;

    private String entityType;    // "Dossier", "Mission", "Document"
    private String entityId;      // le code ou l'UUID de la ressource
    private String details;       // infos lisibles ex: "rapport.pdf"
    private String ipAddress;
}