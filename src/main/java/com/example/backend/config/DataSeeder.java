package com.example.backend.config;

import com.example.backend.entity.ThongBaoTinhHinh;
import com.example.backend.entity.User;
import com.example.backend.enums.PhamVi;
import com.example.backend.repository.ThongBaoTinhHinhRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ThongBaoTinhHinhRepository thongBaoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("----------------------------------------------------------");
            log.info("🛠 Chưa có dữ liệu, đang tiến hành tạo dữ liệu mẫu (Dummy Data)...");
            
            // 1. Tạo Lãnh đạo cấp cao (Xem toàn bộ)
            User thuTruong = new User();
            thuTruong.setName("Thủ Trưởng Nguyễn Văn A");
            thuTruong.setEmail("thutruong@example.com");
            thuTruong.setPassword(passwordEncoder.encode("123456"));
            thuTruong.setRole("ROLE_THU_TRUONG");
            thuTruong.setDonViId(1L); 
            userRepository.save(thuTruong);
            
            User truongPhong = new User();
            truongPhong.setName("Trưởng Phòng Trần Văn B");
            truongPhong.setEmail("truongphong@example.com");
            truongPhong.setPassword(passwordEncoder.encode("123456"));
            truongPhong.setRole("ROLE_TRUONG_PHONG");
            truongPhong.setDonViId(1L); 
            userRepository.save(truongPhong);
            
            // 2. Tạo 2 Cán Bộ Chuyên Trách (Mỗi người quản lý 1 đơn vị)
            User cbct1 = new User();
            cbct1.setName("Cán bộ C - Đơn vị 10");
            cbct1.setEmail("cbct10@example.com");
            cbct1.setPassword(passwordEncoder.encode("123456"));
            cbct1.setRole("ROLE_CBCT");
            cbct1.setDonViId(10L); 
            userRepository.save(cbct1);
            
            User cbct2 = new User();
            cbct2.setName("Cán bộ D - Đơn vị 20");
            cbct2.setEmail("cbct20@example.com");
            cbct2.setPassword(passwordEncoder.encode("123456"));
            cbct2.setRole("ROLE_CBCT");
            cbct2.setDonViId(20L); 
            userRepository.save(cbct2);
            
            // 3. Khởi tạo 2 Thông báo tình hình mẫu
            ThongBaoTinhHinh tb1 = ThongBaoTinhHinh.builder()
                .tieuDe("Thông báo tình hình buôn lậu biên giới tháng 3 (TEST 1)")
                .phanLoaiId(1)
                .donViId(10L) // Của CBCT 1
                .phamVi(PhamVi.NOI_BO_CUC)
                .ngayThongBao(LocalDate.now())
                .noiDung("<p>Báo cáo tình hình xuất nhập khẩu hàng hóa...</p>")
                .ghiChu("Văn bản mât")
                .build();
            thongBaoRepository.save(tb1);
            
            ThongBaoTinhHinh tb2 = ThongBaoTinhHinh.builder()
                .tieuDe("Báo cáo số 123/BC Hải quan điểm X (TEST 2)")
                .phanLoaiId(2)
                .donViId(20L) // Của CBCT 2
                .phamVi(PhamVi.TOAN_NGANH)
                .ngayThongBao(LocalDate.now().minusDays(2))
                .noiDung("<p>Tình hình trong tuần rắt căng thẳng...</p>")
                .ghiChu("")
                .build();
            thongBaoRepository.save(tb2);
            
            log.info("✅ Đã tạo dữ liệu mẫu thành công!");
            log.info("🔑 DÙNG CÁC TÀI KHOẢN SAU ĐỂ TEST TRONG POSTMAN (Mật khẩu chung: 123456):");
            log.info("   1. thutruong@example.com (Quyền: Xem toàn bộ)");
            log.info("   2. truongphong@example.com (Quyền: Xem toàn bộ)");
            log.info("   3. cbct10@example.com (Quyền CRUD - Đơn vị 10)");
            log.info("   4. cbct20@example.com (Quyền CRUD - Đơn vị 20)");
            log.info("----------------------------------------------------------");
        }
    }
}
