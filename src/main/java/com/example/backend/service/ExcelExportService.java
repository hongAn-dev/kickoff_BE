package com.example.backend.service;

import com.example.backend.entity.ThongBaoTinhHinh;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {
    public ByteArrayInputStream exportThongBaoToExcel(List<ThongBaoTinhHinh> dataList) {
        String[] columns = {"ID", "Tiêu đề", "Phân loại", "Phạm vi", "Ngày thông báo", "Người tạo"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách thông báo");

            // Header Font
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);

            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (ThongBaoTinhHinh tb : dataList) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(tb.getId() != null ? tb.getId().toString() : "");
                row.createCell(1).setCellValue(tb.getTieuDe() != null ? tb.getTieuDe() : "");
                row.createCell(2).setCellValue(tb.getPhanLoaiId() != null ? tb.getPhanLoaiId().toString() : "");
                row.createCell(3).setCellValue(tb.getPhamVi() != null ? tb.getPhamVi().toString() : "");
                row.createCell(4).setCellValue(tb.getNgayThongBao() != null ? tb.getNgayThongBao().format(formatter) : "");
                row.createCell(5).setCellValue(tb.getCreatedBy() != null ? tb.getCreatedBy().toString() : "");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tạo file Excel: " + e.getMessage());
        }
    }
}
