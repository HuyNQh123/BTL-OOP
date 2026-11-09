package com.smartenvelope.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * DTO tiếp nhận dữ liệu đăng nhập từ người dùng.
 * Phụ trách bởi: Thành viên 1.
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Vui lòng nhập email hoặc tên đăng nhập")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
