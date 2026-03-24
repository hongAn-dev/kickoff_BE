package com.example.backend.entity;

import com.example.backend.enums.PhamVi;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "thong_bao_tinh_hinh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE thong_bao_tinh_hinh SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false") // Auto-filter deleted records
public class ThongBaoTinhHinh extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "phan_loai_id")
    private Integer phanLoaiId;

    @Column(name = "don_vi_id", nullable = false)
    private Long donViId; // Mapped as Long to align with User.donViId

    @Enumerated(EnumType.STRING)
    @Column(name = "pham_vi", nullable = false)
    private PhamVi phamVi;

    @Column(name = "ngay_thong_bao", nullable = false)
    private LocalDate ngayThongBao;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;
}
