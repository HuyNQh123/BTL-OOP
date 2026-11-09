package com.smartenvelope.service;

import com.smartenvelope.entity.User;
import com.smartenvelope.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

/** Manages ownership verification for the destination email used by notifications. */
@Service
public class NotificationEmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_CODE_ATTEMPTS = 5;

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public NotificationEmailVerificationService(CurrentUserService currentUserService,
            UserRepository userRepository, EmailService emailService) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void sendCode(String email) {
        String normalizedEmail = normalizeGmail(email);
        if (!emailService.isSmtpConfigured()) {
            throw new IllegalStateException("Chưa cấu hình máy chủ gửi email (SMTP). Hãy cấu hình SMTP trước.");
        }

        User user = currentUserService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        if (normalizedEmail.equalsIgnoreCase(user.getNotificationEmail())
                && user.isNotificationEmailVerified()) {
            throw new IllegalArgumentException("Gmail này đã được xác minh.");
        }
        if (user.getNotificationEmailCodeSentAt() != null
                && user.getNotificationEmailCodeSentAt().isAfter(now.minusSeconds(60))) {
            throw new IllegalStateException("Vui lòng đợi 60 giây trước khi yêu cầu mã mới.");
        }

        String code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        user.setNotificationEmail(normalizedEmail);
        user.setNotificationEmailVerified(false);
        user.setNotificationEmailCodeHash(hash(code));
        user.setNotificationEmailCodeExpiresAt(now.plusMinutes(10));
        user.setNotificationEmailCodeSentAt(now);
        user.setNotificationEmailCodeAttempts(0);
        userRepository.save(user);

        if (!emailService.sendNotificationEmailVerificationCode(normalizedEmail, code)) {
            throw new IllegalStateException("Không gửi được mã xác minh. Hãy kiểm tra cấu hình SMTP rồi thử lại.");
        }
    }

    @Transactional
    public boolean verifyCode(String code) {
        User user = currentUserService.getCurrentUser();
        if (user.getNotificationEmail() == null || user.getNotificationEmailCodeHash() == null
                || user.getNotificationEmailCodeExpiresAt() == null
                || user.getNotificationEmailCodeExpiresAt().isBefore(LocalDateTime.now())) {
            clearCode(user);
            userRepository.save(user);
            return false;
        }

        if (user.getNotificationEmailCodeAttempts() >= MAX_CODE_ATTEMPTS) {
            clearCode(user);
            userRepository.save(user);
            return false;
        }

        boolean matches = code != null && code.matches("\\d{6}")
                && MessageDigest.isEqual(hash(code).getBytes(StandardCharsets.US_ASCII),
                        user.getNotificationEmailCodeHash().getBytes(StandardCharsets.US_ASCII));
        if (!matches) {
            int attempts = user.getNotificationEmailCodeAttempts() + 1;
            user.setNotificationEmailCodeAttempts(attempts);
            if (attempts >= MAX_CODE_ATTEMPTS) {
                clearCode(user);
            }
            userRepository.save(user);
            return false;
        }

        user.setNotificationEmailVerified(true);
        clearCode(user);
        userRepository.save(user);
        return true;
    }

    private String normalizeGmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (!normalized.matches("^[a-z0-9._%+-]+@gmail\\.com$")) {
            throw new IllegalArgumentException("Hãy nhập địa chỉ Gmail hợp lệ, ví dụ ten@gmail.com.");
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

    private void clearCode(User user) {
        user.setNotificationEmailCodeHash(null);
        user.setNotificationEmailCodeExpiresAt(null);
        user.setNotificationEmailCodeSentAt(null);
        user.setNotificationEmailCodeAttempts(0);
    }
}
