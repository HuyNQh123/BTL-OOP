package com.smartenvelope.dto.admin;

import com.smartenvelope.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * DTO tiếp nhận dữ liệu tạo mới hoặc cập nhật danh mục hệ thống từ Quản trị viên (Admin).
 * Phụ trách bởi: Thành viên 1.
 */
@Getter
@Setter
@NoArgsConstructor
public class AdminCategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    private String name;

    @NotNull(message = "Loại danh mục (Thu nhập hoặc Chi tiêu) là bắt buộc")
    private CategoryType type;

    @Size(max = 50, message = "Mã biểu tượng icon tối đa 50 ký tự")
    private String icon = "bi-tag";

    @Size(max = 20, message = "Mã màu tối đa 20 ký tự")
    private String color = "#0d6efd";

    private Boolean isActive = true;

    public AdminCategoryRequest(String name, CategoryType type, String icon, String color, Boolean isActive) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.isActive = isActive != null ? isActive : true;
    }

}

