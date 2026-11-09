package com.smartenvelope.dto.user;

import com.smartenvelope.entity.User;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import java.time.LocalDateTime;
/**
 * DTO vận chuyển thông tin người dùng an toàn đến tầng View/Controller (không chứa mật khẩu).
 * Phụ trách bởi: Thành viên 1.
 */
public record UserDTO(Long id, String fullName, String email, Role role, UserStatus status, LocalDateTime createdAt) {
    public UserDTO(User user) {
        this(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getStatus(), user.getCreatedAt());
    }
}

