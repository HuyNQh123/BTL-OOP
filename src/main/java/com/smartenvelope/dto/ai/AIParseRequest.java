package com.smartenvelope.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AIParseRequest(@NotBlank(message = "Vui lòng nhập nội dung giao dịch.")
        @Size(max = 1000, message = "Nội dung tối đa 1.000 ký tự.") String rawPrompt) { }

