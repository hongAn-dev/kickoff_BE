package com.example.backend.repository;

import com.example.backend.entity.ThongBaoAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ThongBaoAuditLogRepository extends JpaRepository<ThongBaoAuditLog, UUID> {
    List<ThongBaoAuditLog> findByRecordIdOrderByChangedAtDesc(UUID recordId);
}
