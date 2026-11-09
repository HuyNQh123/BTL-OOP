package com.smartenvelope.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinanceReminderScheduler {

    private final AlertService alertService;
    private final SavingsGoalService savingsGoalService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendScheduledReminders() {
        alertService.runScheduledReminders();
    }

    @Scheduled(cron = "0 15 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void closeExpiredBudgetPeriods() {
        savingsGoalService.closeExpiredPeriods();
    }
}
