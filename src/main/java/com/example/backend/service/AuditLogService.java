package com.example.backend.service;

import com.example.backend.entity.ThongBaoAuditLog;
import com.example.backend.enums.AuditAction;
import com.example.backend.repository.ThongBaoAuditLogRepository;
import com.example.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final ThongBaoAuditLogRepository auditLogRepository;

    public List<ThongBaoAuditLog> getLogsByRecordId(UUID recordId) {
        return auditLogRepository.findByRecordIdOrderByChangedAtDesc(recordId);
    }

    @Transactional
    public void logChange(UUID recordId, AuditAction action, String fieldName, String oldValue, String newValue) {
        Long currentUserId = getCurrentUserId();

        ThongBaoAuditLog log = ThongBaoAuditLog.builder()
                .recordId(recordId)
                .action(action)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(currentUserId)
                .changedAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return 0L; // Fallback for system actions if any
        }
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}
