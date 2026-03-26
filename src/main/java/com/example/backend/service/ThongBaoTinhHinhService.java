package com.example.backend.service;
import com.example.backend.dto.response.ThongBaoDetailResponse;


import com.example.backend.dto.request.ThongBaoRequest;
import com.example.backend.entity.ThongBaoFile;
import com.example.backend.entity.ThongBaoTinhHinh;
import com.example.backend.enums.PhamVi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface ThongBaoTinhHinhService {
    Page<ThongBaoTinhHinh> getListThongBao(String keyword, Integer phanLoaiId, PhamVi phamVi, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    
    ThongBaoDetailResponse getThongBaoById(UUID id);
    
    ThongBaoTinhHinh createThongBao(ThongBaoRequest request, List<MultipartFile> files);
    
    ThongBaoTinhHinh updateThongBao(UUID id, ThongBaoRequest request, List<MultipartFile> files);
    
    void deleteThongBao(UUID id);

    ThongBaoFile getFileById(UUID fileId);
}
