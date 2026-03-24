package com.example.backend.dto.request;

import com.example.backend.enums.PhamVi;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ThongBaoRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String tieuDe;

    private Integer phanLoaiId;

    @NotNull(message = "Phạm vi không được để trống")
    private PhamVi phamVi;

    @NotNull(message = "Ngày thông báo không được để trống")
    private LocalDate ngayThongBao;

    @NotBlank(message = "Nội dung không được để trống")
    private String noiDung;

    private String ghiChu;
}
