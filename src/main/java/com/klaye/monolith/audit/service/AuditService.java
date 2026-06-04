package com.klaye.monolith.audit.service;

import com.klaye.monolith.audit.entity.AuditAction;
import com.klaye.monolith.audit.entity.AuditLog;
import com.klaye.monolith.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(AuditAction action,
                    String performedBy,
                    Long userId,
                    String entityType,
                    String entityId,
                    String details,
                    String ipAddress) {

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .userId(userId)
                .performedAt(Instant.now())
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(auditLog);
        log.info("[AUDIT] {} | {} | {}/{}", action, performedBy, entityType, entityId);
    }
}