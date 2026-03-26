package com.example.backend.controller;
import com.example.backend.dto.response.ThongBaoDetailResponse;


import com.example.backend.common.ApiResponse;
import com.example.backend.dto.request.ThongBaoRequest;
import com.example.backend.entity.ThongBaoTinhHinh;
import com.example.backend.enums.PhamVi;
import com.example.backend.service.ThongBaoTinhHinhService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import com.example.backend.entity.ThongBaoAuditLog;
import com.example.backend.dto.request.ThongBaoExcelDto;
import com.example.backend.security.UserDetailsImpl;
import com.example.backend.service.AuditLogService;
import com.example.backend.service.ExcelExportService;
import com.example.backend.service.ExcelImportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/thong-bao-tinh-hinh")
@RequiredArgsConstructor
public class ThongBaoTinhHinhController {

    private final ThongBaoTinhHinhService thongBaoService;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;
    private final AuditLogService auditLogService;
    private final com.example.backend.service.FileStorageService fileStorageService;

    // Lấy thông tin User đang đăng nhập bảo mật
    private UserDetailsImpl getCurrentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ThongBaoTinhHinh>>> getListThongBao(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer phanLoaiId,
            @RequestParam(required = false) PhamVi phamVi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 20, sort = "ngayThongBao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ThongBaoTinhHinh> result = thongBaoService.getListThongBao(keyword, phanLoaiId, phamVi, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ThongBaoDetailResponse>> getThongBaoById(@PathVariable UUID id) {
        ThongBaoDetailResponse result = thongBaoService.getThongBaoById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thông báo thành công", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ThongBaoTinhHinh>> createThongBao(
            @RequestPart("data") @Valid ThongBaoRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        System.out.println(">>> RECEIVED CREATE REQUEST: " + request.getTieuDe());
        ThongBaoTinhHinh result = thongBaoService.createThongBao(request, files);
        return ResponseEntity.ok(ApiResponse.success("Tạo thông báo mới thành công", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ThongBaoTinhHinh>> updateThongBao(
            @PathVariable UUID id, 
            @RequestPart("data") @Valid ThongBaoRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        ThongBaoTinhHinh result = thongBaoService.updateThongBao(id, request, files);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông báo thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteThongBao(@PathVariable UUID id) {
        thongBaoService.deleteThongBao(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thông báo thành công", null));
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer phanLoaiId,
            @RequestParam(required = false) PhamVi phamVi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        Pageable pageable = PageRequest.of(0, 10000); 
        List<ThongBaoTinhHinh> dataList = thongBaoService.getListThongBao(keyword, phanLoaiId, phamVi, fromDate, toDate, pageable).getContent();
        
        ByteArrayInputStream in = excelExportService.exportThongBaoToExcel(dataList);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=danh_sach_thong_bao.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("/import/preview")
    public ResponseEntity<ApiResponse<List<ThongBaoExcelDto>>> previewImport(@RequestPart("file") MultipartFile file) {
        List<ThongBaoExcelDto> result = excelImportService.previewImport(file);
        return ResponseEntity.ok(ApiResponse.success("Đọc dử liệu file Excel thành công", result));
    }

    @PostMapping("/import/commit")
    public ResponseEntity<ApiResponse<Void>> commitImport(@RequestBody List<ThongBaoExcelDto> dataList) {
        UserDetailsImpl currentUser = getCurrentUser();
        excelImportService.commitImport(dataList, currentUser.getId(), currentUser.getDonViId());
        return ResponseEntity.ok(ApiResponse.success("Nhập dữ liệu thành công", null));
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<ApiResponse<List<ThongBaoAuditLog>>> getAuditLogs(@PathVariable UUID id) {
        List<ThongBaoAuditLog> logs = thongBaoService.getAuditLogs(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử thay đổi thành công", logs));
    }

    @GetMapping("/attachments/{fileId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(@PathVariable UUID fileId) {
        try {
            com.example.backend.entity.ThongBaoFile fileMeta = thongBaoService.getFileById(fileId);
            org.springframework.core.io.Resource resource = fileStorageService.loadFileAsResource(fileMeta.getFilePath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileMeta.getMimeType() != null ? fileMeta.getMimeType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMeta.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            // Trả về 404 nếu không tìm thấy file vật lý hoặc có lỗi xảy ra
            return ResponseEntity.notFound().build();
        }
    }
}

