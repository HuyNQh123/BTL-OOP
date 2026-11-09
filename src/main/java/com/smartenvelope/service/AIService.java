package com.smartenvelope.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartenvelope.dto.ai.AIParseResponse;
import com.smartenvelope.enums.TransactionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Duration;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses transaction text locally and can use an OpenAI-compatible vision API when configured. */
@Service
public class AIService {
    private static final Pattern AMOUNT = Pattern.compile("(?i)(\\d{1,3}(?:[.,]\\d{3})+(?:,\\d+)?|\\d+(?:[.,]\\d+)?)\\s*(triệu|tr|k|nghìn|ngàn)?");
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public AIService(@Value("${app.ai.api-key:}") String apiKey,
                     @Value("${app.ai.api-url:https://api.openai.com/v1/chat/completions}") String apiUrl,
                     @Value("${app.ai.model:gpt-4o-mini}") String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    public AIParseResponse parse(String input, Collection<String> availableCategories) {
        String text = input == null ? "" : input.trim();
        if (text.isBlank()) return localParse(text, availableCategories);
        if (!apiKey.isBlank()) {
            try { return remoteParse(text, availableCategories); }
            catch (Exception ignored) { /* keep quick entry available if the AI provider is unavailable */ }
        }
        return localParse(text, availableCategories);
    }

    public AIParseResponse parseReceipt(String filename, String contentType, byte[] image,
                                        Collection<String> availableCategories) {
        if (image == null || image.length == 0) throw new IllegalArgumentException("Vui lòng chọn ảnh hóa đơn.");
        if (image.length > 8 * 1024 * 1024) throw new IllegalArgumentException("Ảnh hóa đơn không được vượt quá 8 MB.");
        if (contentType == null || !contentType.matches("image/(jpeg|png|webp)"))
            throw new IllegalArgumentException("Chỉ hỗ trợ ảnh JPG, PNG hoặc WEBP.");
        if (apiKey.isBlank()) throw new IllegalArgumentException("Chưa cấu hình AI_API_KEY để đọc ảnh hóa đơn. Bạn vẫn có thể nhập nội dung giao dịch bằng văn bản.");
        try {
            String dataUrl = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(image);
            String categories = String.join(", ", availableCategories);
            String instruction = "Đọc hóa đơn và trả về duy nhất JSON với amount (số nguyên VND), category (chỉ chọn đúng một tên trong danh mục có sẵn: "
                    + categories + "), note (tên cửa hàng và mô tả ngắn), transactionDate (yyyy-MM-dd nếu đọc được, nếu không dùng ngày hôm nay), type=EXPENSE. Không đoán số tiền nếu không nhìn rõ; confidence từ 0 đến 1.";
            Map<String, Object> imagePart = Map.of("type", "image_url", "image_url", Map.of("url", dataUrl, "detail", "high"));
            Map<String, Object> textPart = Map.of("type", "text", "text", instruction);
            return invoke(List.of(textPart, imagePart));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Không đọc được hóa đơn lúc này. Hãy thử ảnh rõ hơn hoặc nhập giao dịch bằng văn bản.");
        }
    }

    private AIParseResponse remoteParse(String text, Collection<String> categories) throws Exception {
        String today = LocalDate.now(VIETNAM).toString();
        String prompt = "Phân tích câu giao dịch tiếng Việt thành JSON gồm amount (số nguyên VND), category (chọn chính xác một tên trong danh mục: "
                + String.join(", ", categories) + "), type (EXPENSE hoặc INCOME), note (ngắn gọn), transactionDate (yyyy-MM-dd; hiểu sáng nay/hôm qua), confidence (0..1)."
                + " Nếu không rõ ngày dùng " + today + ". Không tự tạo danh mục. Câu: " + text;
        return invoke(List.of(Map.of("type", "text", "text", prompt)));
    }

    private AIParseResponse invoke(List<Map<String, Object>> content) throws IOException, InterruptedException {
        Map<String, Object> payload = Map.of("model", model, "response_format", Map.of("type", "json_object"),
                "messages", List.of(Map.of("role", "user", "content", content)));
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl)).header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json").timeout(Duration.ofSeconds(45))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload))).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IOException("AI provider returned " + response.statusCode());
        JsonNode root = objectMapper.readTree(response.body());
        String contentText = root.path("choices").path(0).path("message").path("content").asText();
        JsonNode parsed = objectMapper.readTree(contentText);
        BigDecimal amount = parsed.path("amount").isNumber() ? parsed.path("amount").decimalValue() : null;
        String category = textOrNull(parsed.path("category").asText());
        String note = textOrNull(parsed.path("note").asText());
        TransactionType type = "INCOME".equalsIgnoreCase(parsed.path("type").asText()) ? TransactionType.INCOME : TransactionType.EXPENSE;
        LocalDate date = parseDate(parsed.path("transactionDate").asText());
        BigDecimal confidence = parsed.path("confidence").isNumber() ? parsed.path("confidence").decimalValue() : new BigDecimal("0.75");
        return new AIParseResponse(amount, category, type, note, confidence, date,
                "AI đã tạo bản nháp. Hãy kiểm tra phong bì, số tiền và ngày trước khi lưu.");
    }

    private AIParseResponse localParse(String input, Collection<String> categories) {
        String text = input == null ? "" : input.trim();
        String lower = text.toLowerCase(Locale.forLanguageTag("vi"));
        Matcher matcher = AMOUNT.matcher(lower);
        BigDecimal amount = null;
        while (matcher.find()) {
            String raw = matcher.group(1);
            String number;
            if (raw.matches("\\d{1,3}(?:[.,]\\d{3})+")) number = raw.replaceAll("[.,]", "");
            else number = raw.replace(',', '.');
            BigDecimal multiplier = switch (Objects.toString(matcher.group(2), "").toLowerCase(Locale.ROOT)) {
                case "triệu", "tr" -> BigDecimal.valueOf(1_000_000);
                case "k", "nghìn", "ngàn" -> BigDecimal.valueOf(1_000);
                default -> BigDecimal.ONE;
            };
            try { amount = new BigDecimal(number).multiply(multiplier).setScale(0, RoundingMode.HALF_UP); }
            catch (NumberFormatException ignored) { amount = null; }
        }
        TransactionType type = containsAny(lower, "lương", "nhận tiền", "được trả", "thu nhập", "bán được")
                ? TransactionType.INCOME : TransactionType.EXPENSE;
        String category = inferCategory(lower, categories);
        LocalDate date = lower.contains("hôm kia") ? LocalDate.now(VIETNAM).minusDays(2)
                : lower.contains("hôm qua") ? LocalDate.now(VIETNAM).minusDays(1) : LocalDate.now(VIETNAM);
        BigDecimal confidence = amount == null ? BigDecimal.ZERO : category == null ? new BigDecimal("0.45") : new BigDecimal("0.65");
        return new AIParseResponse(amount, category, type, textOrNull(text), confidence, date,
                apiKey.isBlank() ? "Bản nháp được phân tích trên thiết bị bằng từ khóa. Cấu hình AI_API_KEY để bật hiểu ngôn ngữ nâng cao và đọc ảnh hóa đơn."
                        : "AI chưa phản hồi; bản nháp được tạo bằng từ khóa. Hãy kiểm tra trước khi lưu.");
    }

    private String inferCategory(String text, Collection<String> categories) {
        String wanted = containsAny(text, "phở", "bún", "cơm", "ăn", "bánh", "đồ ăn", "cafe", "cà phê", "trà sữa", "đi chợ") ? "Ăn uống"
                : containsAny(text, "xăng", "xe bus", "taxi", "grab", "đi xe", "vé xe") ? "Đi lại"
                : containsAny(text, "trọ", "thuê nhà", "tiền nhà", "điện", "nước", "wifi") ? "nhà"
                : containsAny(text, "sách", "học phí", "khóa học", "đồ dùng học") ? "học"
                : containsAny(text, "bệnh viện", "thuốc", "khám", "y tế") ? "y tế"
                : containsAny(text, "cưới", "đám", "mừng", "hiếu hỉ") ? "hiếu hỉ"
                : containsAny(text, "giày", "quần áo", "shopee", "mua", "đi chơi", "phim") ? "mua sắm" : null;
        if (wanted == null || categories == null) return null;
        return categories.stream().filter(Objects::nonNull).filter(name -> {
            String normalized = name.toLowerCase(Locale.forLanguageTag("vi"));
            return switch (wanted) {
                case "Ăn uống" -> normalized.contains("ăn uống");
                case "Đi lại" -> normalized.contains("đi lại");
                case "nhà" -> normalized.contains("nhà") || normalized.contains("tiện ích");
                case "học" -> normalized.contains("học tập");
                case "y tế" -> normalized.contains("y tế") || normalized.contains("sức khỏe");
                case "hiếu hỉ" -> normalized.contains("hiếu hỉ");
                default -> normalized.contains("mua sắm") || normalized.contains("giải trí");
            };
        }).findFirst().orElse(null);
    }

    private boolean containsAny(String text, String... words) { return Arrays.stream(words).anyMatch(text::contains); }
    private String textOrNull(String value) { return value == null || value.isBlank() || "null".equalsIgnoreCase(value) ? null : value.trim(); }
    private LocalDate parseDate(String value) {
        try { return LocalDate.parse(value); } catch (Exception ignored) { return LocalDate.now(VIETNAM); }
    }
}
