package com.example.backend.controller;

import com.example.backend.common.ApiResponse;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.SignupRequest;
import com.example.backend.dto.response.JwtResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
import com.example.backend.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Xác thực người dùng (Kiểm tra email/password với database thông qua UserDetailsService)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // Lưu thông tin xác thực vào Security Context (phiên làm việc hiện tại)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Lấy thông tin User (đã xác thực thành công) từ Context
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Rút trích role (chỉ lấy role đầu tiên cho đơn giản)
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        // Dùng "Máy in thẻ" JwtUtils để in ra chuỗi Token
        String jwt = jwtUtils.generateToken(userDetails.getUsername(), role, userDetails.getDonViId());

        // Lấy thêm Name từ entity gốc (UserDetails không lưu name)
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        String name = user != null ? user.getName() : "";

        // Trả thẻ và thông tin cơ bản về cho Frontend
        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                userDetails.getId(),
                name,
                userDetails.getEmail(),
                role,
                userDetails.getDonViId()
        );

        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", jwtResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi: Email đã được sử dụng!", null));
        }

        // Tạo tài khoản mới, MẬT KHẨU PHẢI ĐƯỢC MÃ HÓA bằng PasswordEncoder
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword())); // Mã hóa (băm) mật khẩu!
        user.setRole(signUpRequest.getRole());
        user.setDonViId(signUpRequest.getDonViId());

        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Đăng ký người dùng thành công!", null));
    }
}
