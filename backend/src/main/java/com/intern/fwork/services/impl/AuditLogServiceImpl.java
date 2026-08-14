package com.intern.fwork.services.impl;

import com.intern.fwork.entities.AuditLog;
import com.intern.fwork.repositories.AuditLogRepository;
import com.intern.fwork.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Uses REQUIRES_NEW to ensure audit log is written even if the calling
     * transaction rolls back. Audit trail must be durable and independent.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UUID userId, String username, String action,
                    String resourceType, UUID resourceId,
                    String detail, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .detail(detail)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }
}
