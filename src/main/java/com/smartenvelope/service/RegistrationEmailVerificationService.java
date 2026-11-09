package com.smartenvelope.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

/** Keeps a short-lived registration email code in the user's HTTP session. */
@Service
public class RegistrationEmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String EMAIL_KEY = "registrationEmail";
    private static final String HASH_KEY = "registrationEmailCodeHash";
    private static final String EXPIRES_KEY = "registrationEmailCodeExpiresAt";
    private static final String SENT_KEY = "registrationEmailCodeSentAt";
    private static final String ATTEMPTS_KEY = "registrationEmailCodeAttempts";
    private static final int MAX_ATTEMPTS = 5;

    private final EmailService emailService;

    public RegistrationEmailVerificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendCode(String email, HttpSession session) {
        String normalizedEmail = normalizeEmail(email);
        if (!emailService.isSmtpConfigured()) {
            throw new IllegalStateException("Chưa cấu hình SMTP. Hãy cấu hình máy chủ email trước khi đăng ký.");
        }

        long now = Instant.now().getEpochSecond();
        Object sentAt = session.getAttribute(SENT_KEY);
        if (sentAt instanceof Long previous && previous > now - 60) {
            throw new IllegalStateException("Vui lòng đợi 60 giây trước khi yêu cầu mã mới.");
        }

        String code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        session.setAttribute(EMAIL_KEY, normalizedEmail);
        session.setAttribute(HASH_KEY, hash(code));
        session.setAttribute(EXPIRES_KEY, now + 600);
        session.setAttribute(SENT_KEY, now);
        session.setAttribute(ATTEMPTS_KEY, 0);

        if (!emailService.sendNotificationEmailVerificationCode(normalizedEmail, code)) {
            clear(session);
            throw new IllegalStateException("Không gửi được mã xác minh. Hãy kiểm tra cấu hình SMTP rồi thử lại.");
        }
    }

    public boolean verify(String email, String code, HttpSession session) {
        String normalizedEmail = normalizeEmail(email);
        Object storedEmail = session.getAttribute(EMAIL_KEY);
        Object storedHash = session.getAttribute(HASH_KEY);
        Object expiresAt = session.getAttribute(EXPIRES_KEY);
        Object attempts = session.getAttribute(ATTEMPTS_KEY);
        long now = Instant.now().getEpochSecond();

        if (!(storedEmail instanceof String expectedEmail) || !expectedEmail.equals(normalizedEmail)
                || !(storedHash instanceof String expectedHash) || !(expiresAt instanceof Long expiry)
                || expiry < now || (attempts instanceof Integer count && count >= MAX_ATTEMPTS)) {
            clear(session);
            return false;
        }

        boolean matches = code != null && code.matches("\\d{6}")
                && MessageDigest.isEqual(hash(code).getBytes(StandardCharsets.US_ASCII),
                        expectedHash.getBytes(StandardCharsets.US_ASCII));
        if (!matches) {
            int newAttempts = attempts instanceof Integer count ? count + 1 : 1;
            session.setAttribute(ATTEMPTS_KEY, newAttempts);
            if (newAttempts >= MAX_ATTEMPTS) {
                clear(session);
            }
        }
        return matches;
    }

    public void clear(HttpSession session) {
        session.removeAttribute(EMAIL_KEY);
        session.removeAttribute(HASH_KEY);
        session.removeAttribute(EXPIRES_KEY);
        session.removeAttribute(SENT_KEY);
        session.removeAttribute(ATTEMPTS_KEY);
    }

    private String normalizeEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (!normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Hãy nhập địa chỉ email hợp lệ.");
        }
        return normalized;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Thiếu thuật toán SHA-256.", exception);
        }
    }
}
