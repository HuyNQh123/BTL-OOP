package com.smartenvelope.service;

import com.smartenvelope.entity.User;
import com.smartenvelope.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class ProfileEmailChangeService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String EMAIL_KEY = "profileEmailChangeAddress";
    private static final String HASH_KEY = "profileEmailChangeCodeHash";
    private static final String EXPIRES_KEY = "profileEmailChangeExpiresAt";
    private static final String SENT_KEY = "profileEmailChangeSentAt";
    private static final String ATTEMPTS_KEY = "profileEmailChangeAttempts";
    private static final int MAX_ATTEMPTS = 5;

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public void sendCode(String email, HttpSession session) {
        String normalizedEmail = normalizeEmail(email);
        User user = currentUserService.getCurrentUser();
        if (normalizedEmail.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Email mới trùng với email tài khoản hiện tại.");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email này đã được một tài khoản khác sử dụng.");
        }
        if (!emailService.isSmtpConfigured()) {
            throw new IllegalStateException("Chưa cấu hình SMTP để gửi mã xác minh.");
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

    @Transactional
    public boolean verifyAndUpdateEmail(String code, HttpSession session) {
        Object pendingEmail = session.getAttribute(EMAIL_KEY);
        Object expectedHash = session.getAttribute(HASH_KEY);
        Object expiry = session.getAttribute(EXPIRES_KEY);
        Object attempts = session.getAttribute(ATTEMPTS_KEY);
        long now = Instant.now().getEpochSecond();
        if (!(pendingEmail instanceof String newEmail) || !(expectedHash instanceof String expectedCodeHash)
                || !(expiry instanceof Long expiresAt) || expiresAt < now
                || (attempts instanceof Integer count && count >= MAX_ATTEMPTS)) {
            clear(session);
            return false;
        }

        boolean matches = code != null && code.matches("\\d{6}")
                && MessageDigest.isEqual(hash(code).getBytes(StandardCharsets.US_ASCII),
                        expectedCodeHash.getBytes(StandardCharsets.US_ASCII));
        if (!matches) {
            int newAttempts = attempts instanceof Integer count ? count + 1 : 1;
            session.setAttribute(ATTEMPTS_KEY, newAttempts);
            if (newAttempts >= MAX_ATTEMPTS) {
                clear(session);
            }
            return false;
        }

        User user = currentUserService.getCurrentUser();
        if (userRepository.existsByEmail(newEmail)) {
            clear(session);
            throw new IllegalArgumentException("Email này đã được một tài khoản khác sử dụng.");
        }

        String oldEmail = user.getEmail();
        user.setEmail(newEmail);
        if (user.isNotificationEmailVerified() && oldEmail.equalsIgnoreCase(user.getNotificationEmail())) {
            user.setNotificationEmail(newEmail);
        }
        userRepository.save(user);
        clear(session);
        return true;
    }

    public String getPendingEmail(HttpSession session) {
        Object email = session.getAttribute(EMAIL_KEY);
        return email instanceof String value ? value : null;
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
        if (normalized.length() > 100 || !normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
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
