package com.example.backend.service.impl;

import com.example.backend.entity.ThongBaoFile;
import com.example.backend.repository.ThongBaoFileRepository;
import com.example.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final ThongBaoFileRepository thongBaoFileRepository;
    
    private final String uploadDir = "uploads";

    @Override
    public String storeFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file " + file.getOriginalFilename() + ". Vui lòng thử lại!", ex);
        }
    }

    @Override
    @Transactional
    public void handleFilesForThongBao(UUID thongBaoId, List<MultipartFile> files, Long uploadedBy) {
        if (files == null || files.isEmpty()) return;
        
        if (files.size() > 5) {
            throw new RuntimeException("Chỉ được đính kèm tối đa 5 file.");
        }

        for (MultipartFile file : files) {
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                throw new RuntimeException("Kích thước file vượt quá 10MB: " + file.getOriginalFilename());
            }

            String filePath = storeFile(file);
            
            ThongBaoFile thongBaoFile = ThongBaoFile.builder()
                    .thongBaoId(thongBaoId)
                    .fileName(StringUtils.cleanPath(file.getOriginalFilename()))
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .uploadedBy(uploadedBy)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            thongBaoFileRepository.save(thongBaoFile);
        }
    }
}
