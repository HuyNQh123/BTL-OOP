package com.smartenvelope.controller.ai;

import com.smartenvelope.dto.ai.AIParseRequest;
import com.smartenvelope.dto.ai.AIParseResponse;
import com.smartenvelope.service.AIService;
import com.smartenvelope.service.BudgetService;
import com.smartenvelope.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller xử lý các yêu cầu phân tích thông minh bằng AI (nhận diện giao dịch từ văn bản, gợi ý tiết kiệm).
 * Phụ trách bởi: Thành viên 5.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AIController {
    private final AIService aiService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;

    @PostMapping("/parse")
    public AIParseResponse parse(@Valid @RequestBody AIParseRequest request) {
        return aiService.parse(request.rawPrompt(), transactionService.getCurrentWorkspaceEnvelopes().stream()
                .map(e -> e.getCategory().getName()).distinct().toList());
    }

    @PostMapping(value = "/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AIParseResponse parseReceipt(@RequestParam("image") MultipartFile image) throws java.io.IOException {
        return aiService.parseReceipt(image.getOriginalFilename(), image.getContentType(), image.getBytes(),
                transactionService.getCurrentWorkspaceEnvelopes().stream()
                        .map(e -> e.getCategory().getName()).distinct().toList());
    }

    @GetMapping("/budget-suggestions")
    public java.util.Map<String, java.math.BigDecimal> budgetSuggestions(@RequestParam java.math.BigDecimal income) {
        return budgetService.suggestAllocations(income);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> badAIRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", exception.getMessage()));
    }
}

