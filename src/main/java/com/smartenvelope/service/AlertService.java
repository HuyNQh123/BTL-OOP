package com.smartenvelope.service;

import com.smartenvelope.entity.*;
import com.smartenvelope.enums.*;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý kiểm tra và phát sinh cảnh báo (cảnh báo khi phong bì đạt 80%,
 * vượt hạn mức, nhắc nhở định kỳ).
 * Phụ trách bởi: Thành viên 4.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertService {

    private static final int DEFAULT_WARNING_THRESHOLD = 80;

    private final CurrentUserService currentUserService;
    private final WorkspaceContextService workspaceContext;
    private final BudgetPeriodRepository budgetRepository;
    private final EnvelopeRepository envelopeRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final UserFinanceSettingsRepository settingsRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public List<Notification> getNotifications() {

        refreshBudgetAlerts();

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(
                currentUserService.getCurrentUserId());
    }

    public long getUnreadNotificationCount() {
        Long userId = currentUserService.getCurrentUserId();
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING)
                + notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.SENT);
    }

    @Transactional
    public void markAsRead(Long notificationId) {

        Long userId = currentUserService.getCurrentUserId();

        Notification notification = notificationRepository.findById(notificationId)
                .filter(item -> item.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo."));

        notification.setStatus(NotificationStatus.READ);
    }

    /** Tạo tối đa một cảnh báo cho mỗi phong bì, loại và ngày. */
    @Transactional
    public void refreshBudgetAlerts() {

        User user = currentUserService.getCurrentUser();

        Long workspaceId = workspaceContext.getCurrentWorkspaceId();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDateTime startOfDay = today.atStartOfDay();

        List<Notification> existing = new ArrayList<>(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));

        List<BudgetPeriod> budgets = budgetRepository.findByWorkspaceIdOrderByYearDescMonthDesc(workspaceId);

        for (BudgetPeriod budget : budgets) {

            if (budget.getStatus() != BudgetStatus.CONFIRMED
                    || budget.getMonth() != today.getMonthValue() || budget.getYear() != today.getYear()) {

                continue;
            }

            for (Envelope envelope : envelopeRepository.findByBudgetPeriodId(budget.getId())) {

                NotificationType type = getAlertType(envelope);

                if (type == null) {

                    continue;
                }

                String title = type == NotificationType.OVERSPENT ? "Đã chạm hạn mức" : "Sắp chạm hạn mức";

                String message = String.format("Phong bì %s đã chi %s trên %s.", envelope.getName(),
                        envelope.getSpentAmount().toPlainString(), envelope.getAllocatedAmount().toPlainString());

                boolean alreadyCreated = existing.stream()
                        .anyMatch(item -> item.getType() == type && item.getTitle().equals(title)
                                && item.getMessage().equals(message) && item.getCreatedAt() != null
                                && !item.getCreatedAt().isBefore(startOfDay));

                if (!alreadyCreated) {

                    Notification notification = new Notification(user, type, title, message, "/budgets");

                    notification.setStatus(NotificationStatus.SENT);

                    Notification saved = notificationRepository.save(notification);
                    existing.add(saved);
                    emailService.sendNotificationEmail(saved);
                }
            }
        }
    }

    /** Runs once per minute; settings control reminder times and reminders are deduplicated per hour/day. */
    @Transactional
    public void runScheduledReminders() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).withSecond(0).withNano(0);
        LocalDate today = now.toLocalDate();

        for (UserFinanceSettings settings : settingsRepository.findAll()) {
            User user = settings.getUser();
            LocalTime budgetReminder = settings.getBudgetReminderTime() == null
                    ? LocalTime.of(10, 0) : settings.getBudgetReminderTime();
            LocalTime stopSpendingReminder = settings.getDailyReminderTime() == null
                    ? LocalTime.of(22, 0) : settings.getDailyReminderTime();

            if (isHourlyReminderTime(now.toLocalTime(), budgetReminder, stopSpendingReminder)
                    && !hasConfirmedBudget(user, today)) {
                LocalDateTime hourStart = now.withMinute(0);
                createReminderUnlessExists(user, NotificationType.BUDGET_REMINDER,
                        "Cần phân bổ ngân sách", "Ngân sách tháng này chưa được phân bổ và xác nhận. Hãy hoàn tất kế hoạch chi tiêu.",
                        "/budgets", hourStart, hourStart.plusHours(1));
            }

            if (now.toLocalTime().getHour() == stopSpendingReminder.getHour()
                    && now.toLocalTime().getMinute() == stopSpendingReminder.getMinute()) {
                LocalDateTime dayStart = today.atStartOfDay();
                createReminderUnlessExists(user, NotificationType.DAILY_CLOSE,
                        "Đến giờ kết thúc chi tiêu", "Hãy dừng các khoản chi trong ngày và rà soát lại giao dịch đã phát sinh.",
                        "/transactions", dayStart, dayStart.plusDays(1));
            }
        }
    }

    private boolean isHourlyReminderTime(LocalTime now, LocalTime reminder, LocalTime stop) {
        if (!now.isBefore(reminder) || !now.isBefore(stop) || now.getMinute() != reminder.getMinute()) return false;
        return (now.toSecondOfDay() - reminder.toSecondOfDay()) % 3600 == 0;
    }

    private boolean hasConfirmedBudget(User user, LocalDate today) {
        return workspaceRepository.findByOwnerIdAndType(user.getId(), WorkspaceType.PERSONAL).stream()
                .map(workspace -> budgetRepository.findByWorkspaceIdAndMonthAndYear(workspace.getId(),
                        today.getMonthValue(), today.getYear()).orElse(null))
                .anyMatch(budget -> budget != null
                        && (budget.getStatus() == BudgetStatus.CONFIRMED || budget.getStatus() == BudgetStatus.CLOSED));
    }

    private void createReminderUnlessExists(User user, NotificationType type, String title, String message,
            String actionUrl, LocalDateTime start, LocalDateTime end) {
        if (!notificationRepository.existsByUserIdAndTypeAndCreatedAtBetween(user.getId(), type, start, end)) {
            Notification notification = new Notification(user, type, title, message, actionUrl);
            notification.setStatus(NotificationStatus.SENT);
            Notification saved = notificationRepository.save(notification);
            emailService.sendNotificationEmail(saved);
        }
    }

    private NotificationType getAlertType(Envelope envelope) {

        BigDecimal allocated = envelope.getAllocatedAmount();

        BigDecimal spent = envelope.getSpentAmount();

        if (allocated == null || spent == null || spent.signum() <= 0) {

            return null;
        }
        if (allocated.signum() <= 0 || spent.compareTo(allocated) >= 0) {

            return NotificationType.OVERSPENT;
        }

        BigDecimal percentUsed = spent.multiply(BigDecimal.valueOf(100))
                .divide(allocated, 2, java.math.RoundingMode.DOWN);

        int threshold = envelope.getWarningThreshold() == null
                ? DEFAULT_WARNING_THRESHOLD
                : envelope.getWarningThreshold();

        return percentUsed.compareTo(BigDecimal.valueOf(threshold)) >= 0
                ? NotificationType.NEAR_LIMIT
                : null;
    }
}
