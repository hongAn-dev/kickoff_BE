package com.example.backend.service.impl;

import com.example.backend.dto.request.ThongBaoExcelDto;
import com.example.backend.dto.request.ThongBaoRequest;
import com.example.backend.enums.PhamVi;
import com.example.backend.service.ExcelImportService;
import com.example.backend.service.ThongBaoTinhHinhService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {

    private final ThongBaoTinhHinhService thongBaoService;

    @Override
    public List<ThongBaoExcelDto> previewImport(MultipartFile file) {
        List<ThongBaoExcelDto> result = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();
        
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Bỏ qua Header

                ThongBaoExcelDto dto = new ThongBaoExcelDto();
                dto.setRowNumber(row.getRowNum() + 1);
                List<String> errors = new ArrayList<>();

                // Cột 0: Tiêu đề
                String tieuDe = dataFormatter.formatCellValue(row.getCell(0));
                dto.setTieuDe(tieuDe);
                if (tieuDe == null || tieuDe.trim().isEmpty()) {
                    errors.add("Tiêu đề không được để trống");
                }

                // Cột 1: Phân loại ID
                String phanLoaiStr = dataFormatter.formatCellValue(row.getCell(1));
                dto.setPhanLoaiId(null);
                if (phanLoaiStr == null || phanLoaiStr.trim().isEmpty()) {
                    errors.add("Phân loại ID không được để trống");
                } else {
                    try {
                        Integer plId = Integer.parseInt(phanLoaiStr);
                        dto.setPhanLoaiId(plId);
                    } catch (NumberFormatException e) {
                        errors.add("Phân loại ID phải là số nguyên");
                    }
                }

                // Cột 2: Phạm Vi
                String phamViStr = dataFormatter.formatCellValue(row.getCell(2));
                dto.setPhamVi(phamViStr);
                if (phamViStr == null || phamViStr.trim().isEmpty()) {
                    errors.add("Phạm vi không được để trống");
                } else {
                    try {
                        PhamVi.valueOf(phamViStr.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add("Phạm vi không hợp lệ (Mẫu: NOI_BO_CUC, TOAN_QUOC)");
                    }
                }

                // Cột 3: Ngày thông báo
                String ngayStr = dataFormatter.formatCellValue(row.getCell(3));
                dto.setNgayThongBao(ngayStr);
                if (ngayStr == null || ngayStr.trim().isEmpty()) {
                    errors.add("Ngày thông báo không được để trống");
                } else {
                    try {
                        LocalDate.parse(ngayStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (DateTimeParseException e) {
                        errors.add("Ngày thông báo cần theo chuẩn dd/MM/yyyy");
                    }
                }

                // Cột 4, 5: Nội dung, Ghi chú
                dto.setNoiDung(dataFormatter.formatCellValue(row.getCell(4)));
                dto.setGhiChu(dataFormatter.formatCellValue(row.getCell(5)));

                dto.setErrors(errors);
                dto.setIsValid(errors.isEmpty());
                result.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }

        return result;
    }

    @Override
    public void commitImport(List<ThongBaoExcelDto> dataList, Long userId, Long donViId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (ThongBaoExcelDto dto : dataList) {
            if (dto.getIsValid() == null || !dto.getIsValid()) continue; // Bỏ qua dữ liệu lỗi

            ThongBaoRequest req = new ThongBaoRequest();
            req.setTieuDe(dto.getTieuDe());
            req.setPhanLoaiId(dto.getPhanLoaiId());
            req.setPhamVi(PhamVi.valueOf(dto.getPhamVi().trim().toUpperCase()));
            req.setNgayThongBao(LocalDate.parse(dto.getNgayThongBao(), formatter));
            req.setNoiDung(dto.getNoiDung());
            req.setGhiChu(dto.getGhiChu());

            // Tái sử dụng service hiện tại để đảm bảo Audit Log / Quyền hoạt động chuẩn
            thongBaoService.createThongBao(req, null);
        }
    }
}
