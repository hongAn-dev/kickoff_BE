package com.example.backend.entity;

import com.example.backend.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "thong_bao_tinh_hinh_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongBaoAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "record_id", nullable = false)
    private UUID recordId; // References ThongBaoTinhHinh.id

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "changed_by", nullable = false)
    private Long changedBy; // References User.id

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
