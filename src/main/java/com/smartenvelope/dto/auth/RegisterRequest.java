package com.smartenvelope.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * DTO tiếp nhận dữ liệu đăng ký tài khoản mới từ người dùng.
 * Phụ trách bởi: Thành viên 1.
 */
@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(max = 100, message = "Họ và tên không vượt quá 100 ký tự")
    private String fullName;

    @Size(max = 50, message = "Tên đăng nhập không vượt quá 50 ký tự")
    private String username;

    @NotBlank(message = "Vui lòng nhập địa chỉ email")
    @Email(message = "Địa chỉ email không đúng định dạng")
    @Size(max = 100, message = "Email không vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Vui lòng xác minh email bằng mã đã gửi")
    private String emailVerificationCode;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 6, max = 50, message = "Mật khẩu phải chứa ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng xác nhận lại mật khẩu")
    private String confirmPassword;

    public RegisterRequest(String fullName, String username, String email, String password, String confirmPassword) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

}
