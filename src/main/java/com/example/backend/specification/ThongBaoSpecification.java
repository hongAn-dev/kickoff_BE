package com.example.backend.specification;

import com.example.backend.entity.ThongBaoTinhHinh;
import com.example.backend.enums.PhamVi;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class ThongBaoSpecification {

    // 1. Tìm kiếm theo tiêu đề (Search text)
    public static Specification<ThongBaoTinhHinh> hasTieuDe(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction(); // Không lọc gì cả
            }
            // LIKE %keyword% (Bỏ qua viết hoa viết thường)
            return cb.like(cb.lower(root.get("tieuDe")), "%" + keyword.toLowerCase() + "%");
        };
    }

    // 2. Lọc theo Phân loại
    public static Specification<ThongBaoTinhHinh> hasPhanLoai(Integer phanLoaiId) {
        return (root, query, cb) -> {
            if (phanLoaiId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("phanLoaiId"), phanLoaiId);
        };
    }

    // 3. Lọc theo Phạm vi
    public static Specification<ThongBaoTinhHinh> hasPhamVi(PhamVi phamVi) {
        return (root, query, cb) -> {
            if (phamVi == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("phamVi"), phamVi);
        };
    }

    // 4. Lọc theo khoảng thời gian (Từ ngày - Đến ngày)
    public static Specification<ThongBaoTinhHinh> isBetweenDates(LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            if (fromDate == null && toDate == null) return cb.conjunction();
            if (fromDate != null && toDate != null) return cb.between(root.get("ngayThongBao"), fromDate, toDate);
            if (fromDate != null) return cb.greaterThanOrEqualTo(root.get("ngayThongBao"), fromDate);
            return cb.lessThanOrEqualTo(root.get("ngayThongBao"), toDate);
        };
    }

    // 5. BẢO MẬT & PHÂN QUYỀN: Chỉ cho phép xem danh sách đơn vị cụ thể
    public static Specification<ThongBaoTinhHinh> hasDonViIn(List<Long> donViIds) {
        return (root, query, cb) -> {
            if (donViIds == null || donViIds.isEmpty()) {
                return cb.conjunction();
            }
            // Tạo câu lệnh: WHERE donViId IN (id1, id2, ...)
            return root.get("donViId").in(donViIds);
        };
    }
}
