package com.smartenvelope.dto.account;

import com.smartenvelope.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank(message = "Vui lòng nhập tên tài khoản.")
        @Size(max = 100, message = "Tên tài khoản tối đa 100 ký tự.")
        String name,
        @NotNull(message = "Vui lòng chọn loại tài khoản.")
        AccountType type,
        @NotNull(message = "Vui lòng nhập số dư ban đầu.")
        @DecimalMin(value = "0", message = "Số dư không được âm.")
        BigDecimal initialBalance) {
}
