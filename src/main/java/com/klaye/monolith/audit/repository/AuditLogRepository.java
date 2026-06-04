package com.klaye.monolith.audit.repository;

import com.klaye.monolith.audit.entity.AuditAction;
import com.klaye.monolith.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(
            String entityType, String entityId);

    List<AuditLog> findByPerformedByOrderByPerformedAtDesc(
            String performedBy);

    List<AuditLog> findByActionOrderByPerformedAtDesc(
            AuditAction action);

    List<AuditLog> findByPerformedAtBetweenOrderByPerformedAtDesc(
            Instant from, Instant to);

    List<AuditLog> findAllByOrderByPerformedAtDesc();
}