package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "thong_bao_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongBaoFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "thong_bao_id", nullable = false)
    private UUID thongBaoId; // References ThongBaoTinhHinh.id

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy; // References User.id

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
