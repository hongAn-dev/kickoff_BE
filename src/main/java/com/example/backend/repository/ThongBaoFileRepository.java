package com.example.backend.repository;

import com.example.backend.entity.ThongBaoFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ThongBaoFileRepository extends JpaRepository<ThongBaoFile, UUID> {
    List<ThongBaoFile> findByThongBaoId(UUID thongBaoId);
}
