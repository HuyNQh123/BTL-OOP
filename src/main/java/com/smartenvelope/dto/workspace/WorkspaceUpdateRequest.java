package com.smartenvelope.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * DTO nhận dữ liệu cập nhật thông tin Quỹ tài chính (Workspace).
 * Phụ trách bởi: Thành viên 1.
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkspaceUpdateRequest {

    @NotBlank(message = "Tên Quỹ tài chính không được để trống")
    @Size(max = 100, message = "Tên Quỹ không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

    public WorkspaceUpdateRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

}

