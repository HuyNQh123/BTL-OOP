package com.smartenvelope.dto.workspace;

import com.smartenvelope.enums.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * DTO nhận dữ liệu gửi lời mời tham gia Quỹ qua email.
 * Phụ trách bởi: Thành viên 1.
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkspaceInviteRequest {

    @NotBlank(message = "Địa chỉ email không được để trống")
    @Email(message = "Email không đúng định dạng hợp lệ")
    private String email;

    @NotNull(message = "Vai trò không được để trống")
    private MemberRole role = MemberRole.VIEWER;

    public WorkspaceInviteRequest(String email, MemberRole role) {
        this.email = email;
        this.role = role != null ? role : MemberRole.VIEWER;
    }

}

