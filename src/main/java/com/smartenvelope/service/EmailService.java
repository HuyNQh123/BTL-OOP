package com.smartenvelope.service;

import com.smartenvelope.entity.Notification;
import com.smartenvelope.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends application notifications and workspace invitations by email when SMTP is configured. */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String smtpHost;
    private final String fromAddress;
    private final String baseUrl;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String smtpHost,
            @Value("${app.mail.from:}") String fromAddress,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.smtpHost = smtpHost;
        this.fromAddress = fromAddress;
        this.baseUrl = baseUrl;
    }

    public void sendNotificationEmail(Notification notification) {
        if (notification == null || notification.getUser() == null) {
            return;
        }
        User recipient = notification.getUser();
        if (!recipient.isNotificationEmailVerified() || recipient.getNotificationEmail() == null
                || recipient.getNotificationEmail().isBlank()) {
            log.info("Bỏ qua email thông báo cho user {} vì chưa xác minh email nhận thông báo.", recipient.getId());
            return;
        }
        String actionUrl = notification.getActionUrl();
        String body = notification.getMessage();
        if (actionUrl != null && !actionUrl.isBlank()) {
            body += "\n\nMở nội dung liên quan: " + absoluteUrl(actionUrl);
        }
        send(recipient.getNotificationEmail(), notification.getTitle(), body);
    }

    public boolean isSmtpConfigured() {
        return smtpHost != null && !smtpHost.isBlank();
    }

    public boolean sendNotificationEmailVerificationCode(String toEmail, String code) {
        String body = "Mã xác minh email SmartEnvelope của bạn là: " + code
                + "\nMã có hiệu lực trong 10 phút. Nếu bạn không yêu cầu mã này, hãy bỏ qua email.";
        return send(toEmail, "Mã xác minh email SmartEnvelope", body);
    }

    public void sendWorkspaceInvitationEmail(String toEmail, String workspaceName,
            String inviterName, String inviteUrl) {
        String subject = "Lời mời tham gia Quỹ tài chính " + workspaceName;
        String body = inviterName + " đã mời bạn tham gia Quỹ tài chính \"" + workspaceName
                + "\".\n\nMở lời mời: " + absoluteUrl(inviteUrl);
        send(toEmail, subject, body);
    }

    private boolean send(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isBlank()) {
            return false;
        }
        if (smtpHost == null || smtpHost.isBlank()) {
            log.info("Chưa cấu hình SMTP; bỏ qua gửi email tới {}. Thêm SMTP_HOST và thông tin SMTP vào .env.",
                    toEmail);
            return false;
        }

        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                log.warn("Không tìm thấy JavaMailSender; email tới {} chưa được gửi.", toEmail);
                return false;
            }
            SimpleMailMessage mail = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                mail.setFrom(fromAddress);
            }
            mail.setTo(toEmail);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
            log.info("Đã gửi email thông báo tới {}.", toEmail);
            return true;
        } catch (RuntimeException exception) {
            // Mail delivery must not roll back a saved in-app notification or block a scheduler.
            log.warn("Gửi email tới {} thất bại: {}", toEmail, exception.getMessage());
            return false;
        }
    }

    private String absoluteUrl(String path) {
        if (path == null || path.isBlank()) {
            return baseUrl;
        }
        return baseUrl.replaceAll("/$", "") + (path.startsWith("/") ? path : "/" + path);
    }
}
