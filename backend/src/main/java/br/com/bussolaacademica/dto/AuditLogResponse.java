package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(Long id, LocalDateTime createdAt, Long userId, String userEmail, String action,
                               String resource, String details, String ipAddress, boolean success) {

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getCreatedAt(), log.getUserId(), log.getUserEmail(),
                log.getAction().name(), log.getResource(), log.getDetails(), log.getIpAddress(), log.isSuccess());
    }
}
