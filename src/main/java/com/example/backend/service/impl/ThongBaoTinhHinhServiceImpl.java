package com.example.backend.service.impl;

import com.example.backend.dto.request.ThongBaoRequest;
import com.example.backend.entity.ThongBaoTinhHinh;
import com.example.backend.enums.AuditAction;
import com.example.backend.enums.PhamVi;
import com.example.backend.repository.ThongBaoTinhHinhRepository;
import com.example.backend.security.UserDetailsImpl;
import com.example.backend.service.AuditLogService;
import com.example.backend.service.FileStorageService;
import com.example.backend.service.ThongBaoTinhHinhService;
import com.example.backend.dto.response.ThongBaoDetailResponse;
import com.example.backend.dto.response.ThongBaoFileResponse;
import com.example.backend.repository.ThongBaoFileRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.entity.User;
import com.example.backend.entity.ThongBaoFile;
import com.example.backend.specification.ThongBaoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.backend.entity.ThongBaoAuditLog;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThongBaoTinhHinhServiceImpl implements ThongBaoTinhHinhService {

    private final ThongBaoTinhHinhRepository thongBaoRepository;
    private final AuditLogService auditLogService;
    private final FileStorageService fileStorageService;
    private final ThongBaoFileRepository fileRepository;
    private final UserRepository userRepository;

    // Lấy thông tin người dùng đang gọi API từ JWT
    private UserDetailsImpl getCurrentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Lấy Role từ JWT
    private String getCurrentUserRole() {
        return getCurrentUser().getAuthorities().iterator().next().getAuthority();
    }

    @Override
    public Page<ThongBaoTinhHinh> getListThongBao(String keyword, Integer phanLoaiId, PhamVi phamVi, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        UserDetailsImpl currentUser = getCurrentUser();
        String role = getCurrentUserRole();

        // 1. Lắp ráp các điều kiện tìm kiếm cơ bản (Lego)
        Specification<ThongBaoTinhHinh> spec = Specification.where((Specification<ThongBaoTinhHinh>) ThongBaoSpecification.hasTieuDe(keyword))
                .and(ThongBaoSpecification.hasPhanLoai(phanLoaiId))
                .and(ThongBaoSpecification.hasPhamVi(phamVi))
                .and(ThongBaoSpecification.isBetweenDates(fromDate, toDate));

        // 2. PHÂN QUYỀN TRUY CẬP DỮ LIỆU DỰA TRÊN ROLE
        if (role.equals("ROLE_CBCT")) {
            // Cán bộ chuyên trách: Chỉ được xem thông báo của đơn vị mình
            spec = spec.and(ThongBaoSpecification.hasDonViIn(Collections.singletonList(currentUser.getDonViId())));
        } else if (role.equals("ROLE_TRUONG_PHONG") || role.equals("ROLE_THU_TRUONG")) {
            spec = spec.and(Specification.where((Specification<ThongBaoTinhHinh>) null)); // Không ràng buộc donViId
        }

        return thongBaoRepository.findAll(spec, pageable);
    }

    @Override
    public ThongBaoDetailResponse getThongBaoById(UUID id) {
        ThongBaoTinhHinh thongBao = thongBaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo rủi ro với ID: " + id));
        
        // Kiểm tra quyền xem chi tiết
        checkViewPermission(thongBao);

        // Lấy tên người tạo
        String creatorName = "Chưa xác định";
        if (thongBao.getCreatedBy() != null) {
            creatorName = userRepository.findById(thongBao.getCreatedBy())
                    .map(User::getName)
                    .orElse("ID: " + thongBao.getCreatedBy());
        }

        // Mock Tên đơn vị (Tương tự Creator, search từ donViId nếu có bảng)
        String donViName = "Đơn vị ID: " + thongBao.getDonViId();
        if (thongBao.getDonViId() == 1L) donViName = "Cục Hải Quan (Tổng cục)";
        else if (thongBao.getDonViId() == 10L) donViName = "Chi Cục Hải Quan Đơn vị 10";
        else if (thongBao.getDonViId() == 20L) donViName = "Chi Cục Hải Quan Đơn vị 20";

        // Lấy danh sách File
        List<ThongBaoFileResponse> files = fileRepository.findByThongBaoId(id).stream()
                .map(f -> ThongBaoFileResponse.builder()
                        .id(f.getId())
                        .fileName(f.getFileName())
                        .filePath(f.getFilePath())
                        .fileSize(f.getFileSize())
                        .mimeType(f.getMimeType())
                        .uploadedAt(f.getUploadedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ThongBaoDetailResponse.builder()
                .data(thongBao)
                .creatorName(creatorName)
                .donViName(donViName)
                .files(files)
                .build();
    }

    @Override
    @Transactional
    public ThongBaoTinhHinh createThongBao(ThongBaoRequest request, List<MultipartFile> files) {
        String role = getCurrentUserRole();
        if (!role.equals("ROLE_CBCT") && !role.equals("ROLE_TRUONG_PHONG")) {
            throw new AccessDeniedException("Bạn không có quyền tạo thông báo mới");
        }

        UserDetailsImpl currentUser = getCurrentUser();
        
        ThongBaoTinhHinh thongBao = ThongBaoTinhHinh.builder()
                .tieuDe(request.getTieuDe())
                .phanLoaiId(request.getPhanLoaiId())
                .donViId(currentUser.getDonViId()) 
                .phamVi(request.getPhamVi())
                .ngayThongBao(request.getNgayThongBao())
                .noiDung(request.getNoiDung())
                .ghiChu(request.getGhiChu())
                .build();

        ThongBaoTinhHinh saved = thongBaoRepository.save(thongBao);

        // Xử lý lưu File
        fileStorageService.handleFilesForThongBao(saved.getId(), files, currentUser.getId());

        auditLogService.logChange(saved.getId(), AuditAction.CREATE, "ALL", null, "Bản ghi được tạo mới");

        return saved;
    }

    @Override
    @Transactional
    public ThongBaoTinhHinh updateThongBao(UUID id, ThongBaoRequest request, List<MultipartFile> files) {
        String role = getCurrentUserRole();
        if (!role.equals("ROLE_CBCT")) {
            throw new AccessDeniedException("Chỉ Cán bộ chuyên trách mới có quyền cập nhật thông báo");
        }

        ThongBaoTinhHinh existing = findEntityById(id);
        
        checkOwnershipPermission(existing);

        trackChangesAndLog(existing, request);

        existing.setTieuDe(request.getTieuDe());
        existing.setPhanLoaiId(request.getPhanLoaiId());
        existing.setPhamVi(request.getPhamVi());
        existing.setNgayThongBao(request.getNgayThongBao());
        existing.setNoiDung(request.getNoiDung());
        existing.setGhiChu(request.getGhiChu());

        // Bổ sung thêm files mới vào hồ sơ
        fileStorageService.handleFilesForThongBao(existing.getId(), files, getCurrentUser().getId());

        return thongBaoRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteThongBao(UUID id) {
        String role = getCurrentUserRole();
        if (!role.equals("ROLE_CBCT")) {
            throw new AccessDeniedException("Chỉ Cán bộ chuyên trách mới có quyền xóa thông báo");
        }

        ThongBaoTinhHinh existing = findEntityById(id);
        
        // CHỐT CHẶN BẢO MẬT: Phải thuộc đúng Đơn vị mới được xóa
        checkOwnershipPermission(existing);
        
        thongBaoRepository.delete(existing); // Sẽ tự động trigger Soft Delete (@SQLDelete)
        
        // Ghi Audit Log DELETE
        auditLogService.logChange(existing.getId(), AuditAction.DELETE, "is_deleted", "false", "true");
    }
    
    // --- CÁC HÀM HỖ TRỢ / UNTILITY ---

    private ThongBaoTinhHinh findEntityById(UUID id) {
        return thongBaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo rủi ro với ID: " + id));
    }

    private void checkViewPermission(ThongBaoTinhHinh thongBao) {
        UserDetailsImpl currentUser = getCurrentUser();
        String role = getCurrentUserRole();
        
        // Nếu là CBCT, buộc hệ thống kiểm tra thông báo đó có phải của đơn vị mình không
        if (role.equals("ROLE_CBCT") && !thongBao.getDonViId().equals(currentUser.getDonViId())) {
            throw new RuntimeException("Bạn không có quyền xem thông báo của đơn vị khác."); // Thường là ForbiddenException 403
        }
    }

    private void checkOwnershipPermission(ThongBaoTinhHinh thongBao) {
        UserDetailsImpl currentUser = getCurrentUser();
        // Cả CBCT và Trưởng phòng/Thủ trưởng đều không được sửa chéo (trừ khi có rule khác)
        // Rule hiện tại: Update/Delete phải check thongBao.donViId == currentUser.donViId
        if (!thongBao.getDonViId().equals(currentUser.getDonViId())) {
            throw new RuntimeException("Truy cập bị từ chối: Bạn không phải chủ sở hữu của thông báo này.");
        }
    }

    private void trackChangesAndLog(ThongBaoTinhHinh oldData, ThongBaoRequest newData) {
        UUID id = oldData.getId();
        
        if (!Objects.equals(oldData.getTieuDe(), newData.getTieuDe())) {
            auditLogService.logChange(id, AuditAction.UPDATE, "tieu_de", oldData.getTieuDe(), newData.getTieuDe());
        }
        if (!Objects.equals(oldData.getPhanLoaiId(), newData.getPhanLoaiId())) {
            auditLogService.logChange(id, AuditAction.UPDATE, "phan_loai_id", String.valueOf(oldData.getPhanLoaiId()), String.valueOf(newData.getPhanLoaiId()));
        }
        if (!Objects.equals(oldData.getPhamVi(), newData.getPhamVi())) {
            auditLogService.logChange(id, AuditAction.UPDATE, "pham_vi", String.valueOf(oldData.getPhamVi()), String.valueOf(newData.getPhamVi()));
        }
        if (!Objects.equals(oldData.getNgayThongBao(), newData.getNgayThongBao())) {
            auditLogService.logChange(id, AuditAction.UPDATE, "ngay_thong_bao", String.valueOf(oldData.getNgayThongBao()), String.valueOf(newData.getNgayThongBao()));
        }
        if (!Objects.equals(oldData.getNoiDung(), newData.getNoiDung())) {
            // Nội dung thường là Text HTML dài, ghi log có thể gây tốn dung lượng DB, tuỳ chiến lược dự án.
            auditLogService.logChange(id, AuditAction.UPDATE, "noi_dung", "<nội dung HTML cũ>", "<nội dung HTML mới>");
        }
        if (!Objects.equals(oldData.getGhiChu(), newData.getGhiChu())) {
            auditLogService.logChange(id, AuditAction.UPDATE, "ghi_chu", oldData.getGhiChu(), newData.getGhiChu());
        }
    }

    @Override
    public ThongBaoFile getFileById(UUID fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy file đính kèm với ID: " + fileId));
    }

    @Override
    public List<ThongBaoAuditLog> getAuditLogs(UUID id) {
        String role = getCurrentUserRole();
        if (!role.equals("ROLE_TRUONG_PHONG") && !role.equals("ROLE_THU_TRUONG")) {
            throw new AccessDeniedException("Bạn không có quyền xem lịch sử thay đổi (Yêu cầu quyền Trưởng phòng)");
        }
        return auditLogService.getLogsByRecordId(id);
    }
}
