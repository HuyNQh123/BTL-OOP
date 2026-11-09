package com.smartenvelope.dto.admin;

import com.smartenvelope.entity.User;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO đóng gói thông tin an toàn của người dùng hiển thị cho Quản trị viên (Admin).
 * Tuyệt đối không bao gồm password hash, reset token hay thông tin xác thực nhạy cảm.
 * Phụ trách bởi: Thành viên 1.
 */
@Getter
@NoArgsConstructor
public class AdminUserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int workspaceCount;

    public AdminUserResponse(User user, int workspaceCount) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.avatarUrl = user.getAvatarUrl();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.workspaceCount = workspaceCount;
    }

}

