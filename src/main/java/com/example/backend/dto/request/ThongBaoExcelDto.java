package com.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongBaoExcelDto {
    private Integer rowNumber;
    
    private String tieuDe;
    private Integer phanLoaiId;
    @JsonProperty("hinhThucText")
    private String phamVi;
    
    @JsonProperty("ngayPhatSinh")
    private String ngayThongBao;

    private String noiDung;
    private String ghiChu;
    
    @Builder.Default
    private Boolean isValid = true;
    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
