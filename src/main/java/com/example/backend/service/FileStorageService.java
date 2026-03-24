package com.example.backend.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface FileStorageService {
    String storeFile(MultipartFile file);
    void handleFilesForThongBao(UUID thongBaoId, List<MultipartFile> files, Long uploadedBy);
}
