package com.example.backend.dto.response;

import com.example.backend.entity.ThongBaoTinhHinh;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ThongBaoDetailResponse {
    private ThongBaoTinhHinh data;
    private String creatorName;
    private String donViName;
    private List<ThongBaoFileResponse> files;
}
