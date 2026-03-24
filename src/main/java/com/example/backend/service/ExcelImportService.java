package com.example.backend.service;

import com.example.backend.dto.request.ThongBaoExcelDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelImportService {
    List<ThongBaoExcelDto> previewImport(MultipartFile file);
    void commitImport(List<ThongBaoExcelDto> dataList, Long userId, Long donViId);
}
