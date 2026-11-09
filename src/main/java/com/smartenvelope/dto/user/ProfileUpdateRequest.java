package com.smartenvelope.dto.user;

import com.smartenvelope.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "Vui lòng nhập họ và tên.")
        @Size(max = 100, message = "Họ và tên không vượt quá 100 ký tự.")
        String fullName,

        @Size(max = 20, message = "Số điện thoại không vượt quá 20 ký tự.")
        String phone) {

    public static ProfileUpdateRequest from(User user) {
        return new ProfileUpdateRequest(user.getFullName(), user.getPhone());
    }
}
