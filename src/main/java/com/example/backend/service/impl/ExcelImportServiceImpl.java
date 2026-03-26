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

import org.springframework.transaction.annotation.Transactional;
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
            
            int lastRow = sheet.getLastRowNum();
            log.info("Sheet has {} total rows (0-indexed)", lastRow);

            for (int i = 0; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String firstCell = dataFormatter.formatCellValue(row.getCell(0)).toLowerCase();
                // Bỏ qua dòng tiêu đề nếu chứa các từ khóa tiêu đề hoặc trống hoàn toàn
                if (i == 0 && (firstCell.contains("tiêu đề") || firstCell.contains("stt"))) {
                    log.info("Skipping header row at index 0");
                    continue;
                }
                
                if (firstCell.isEmpty() && dataFormatter.formatCellValue(row.getCell(1)).isEmpty()) {
                    continue; // Bỏ qua dòng trắng
                }

                ThongBaoExcelDto dto = new ThongBaoExcelDto();
                dto.setRowNumber(i + 1);
                List<String> errors = new ArrayList<>();

                log.info("Processing Excel Row {} (POI Row {}):", dto.getRowNumber(), i);

                // Cột 0: Tiêu đề
                String tieuDe = dataFormatter.formatCellValue(row.getCell(0));
                dto.setTieuDe(tieuDe);
                if (tieuDe == null || tieuDe.trim().isEmpty() || tieuDe.contains("(Để trống)") || tieuDe.contains("(?? tr?ng)")) {
                    errors.add("Tiêu đề không được để trống");
                }

                // Cột 1: Phân loại ID
                String phanLoaiStr = dataFormatter.formatCellValue(row.getCell(1));
                dto.setPhanLoaiId(null);
                if (phanLoaiStr == null || phanLoaiStr.trim().isEmpty()) {
                    errors.add("Phân loại ID không được để trống");
                } else {
                    try {
                        Integer plId = Integer.parseInt(phanLoaiStr.replaceAll("[^0-9]", ""));
                        dto.setPhanLoaiId(plId);
                    } catch (Exception e) {
                        errors.add("Phân loại ID phải là số nguyên (Giá trị hiện tại: " + phanLoaiStr + ")");
                    }
                }

                // Cột 2: Phạm Vi
                String phamViStr = dataFormatter.formatCellValue(row.getCell(2));
                dto.setPhamVi(phamViStr);
                if (phamViStr == null || phamViStr.trim().isEmpty()) {
                    errors.add("Phạm vi không được để trống");
                } else {
                    try {
                        String normalized = phamViStr.trim().toUpperCase();
                        if (normalized.equals("TOAN_QUOC") || normalized.equals("TOÀN QUỐC")) {
                            normalized = "TOAN_NGANH";
                        }
                        PhamVi.valueOf(normalized);
                    } catch (IllegalArgumentException e) {
                        errors.add("Phạm vi không hợp lệ: " + phamViStr);
                    }
                }

                // Cột 3: Ngày thông báo
                String ngayStr = dataFormatter.formatCellValue(row.getCell(3));
                dto.setNgayThongBao(ngayStr);
                
                log.info(" - Tieu De: '{}' | Phan Loai: '{}' | Pham Vi: '{}' | Ngay: '{}'", tieuDe, phanLoaiStr, phamViStr, ngayStr);

                if (ngayStr == null || ngayStr.trim().isEmpty()) {
                    errors.add("Ngày thông báo không được để trống");
                } else {
                    try {
                        String cleanNgay = ngayStr.trim();
                        // Thử parse với dd/MM/yyyy
                        LocalDate.parse(cleanNgay, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (DateTimeParseException e) {
                        errors.add("Ngày thông báo sai chuẩn dd/MM/yyyy: " + ngayStr);
                    }
                }

                // Cột 4, 5: Nội dung, Ghi chú
                dto.setNoiDung(dataFormatter.formatCellValue(row.getCell(4)));
                dto.setGhiChu(dataFormatter.formatCellValue(row.getCell(5)));

                dto.setErrors(errors);
                boolean isValid = errors.isEmpty();
                dto.setValid(isValid);
                dto.setIsValid(isValid);
                dto.setStatus(isValid ? "HỢP LỆ" : "KHÔNG HỢP LỆ");
                
                if (!isValid) {
                    log.error("Row {} is INVALID. Errors: {}", dto.getRowNumber(), errors);
                }

                result.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional
    public void commitImport(List<ThongBaoExcelDto> dataList, Long userId, Long donViId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int successCount = 0;
        int skipCount = 0;

        log.info("Bắt đầu commit import Excel cho userId: {}, donViId: {}, tổng số dòng: {}", userId, donViId, dataList.size());
        
        for (ThongBaoExcelDto dto : dataList) {
            // Sửa logic: Chỉ bỏ qua nếu valid là FALSE. Nếu là null (người dùng không gửi) thì vẫn coi là true.
            if (Boolean.FALSE.equals(dto.getValid())) {
                log.warn("Bỏ qua dòng {} do valid = false", dto.getRowNumber());
                skipCount++;
                continue;
            }

            try {
                ThongBaoRequest req = new ThongBaoRequest();
                req.setTieuDe(dto.getTieuDe());
                req.setPhanLoaiId(dto.getPhanLoaiId());
                
                // Bổ sung null check để tránh NPE khi người dùng gửi thiếu field
                if (dto.getPhamVi() != null) {
                    req.setPhamVi(PhamVi.valueOf(dto.getPhamVi().trim().toUpperCase()));
                }
                
                if (dto.getNgayThongBao() != null) {
                    req.setNgayThongBao(LocalDate.parse(dto.getNgayThongBao(), formatter));
                }
                
                req.setNoiDung(dto.getNoiDung());
                req.setGhiChu(dto.getGhiChu());

                // Tái sử dụng service hiện tại để đảm bảo Audit Log / Quyền hoạt động chuẩn
                thongBaoService.createThongBao(req, null);
                successCount++;
            } catch (Exception e) {
                log.error("Lỗi khi xử lý dòng {}: {}", dto.getRowNumber(), e.getMessage());
                throw new RuntimeException("Lỗi tại dòng " + dto.getRowNumber() + ": " + e.getMessage());
            }
        }
        
        log.info("Hoàn tất commit import: Thành công {} dòng, Bỏ qua {} dòng", successCount, skipCount);
    }
}

