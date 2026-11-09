package com.smartenvelope.service;

import com.smartenvelope.dto.settings.FinanceSettingsRequest;
import com.smartenvelope.entity.UserFinanceSettings;
import com.smartenvelope.repository.UserFinanceSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceSettingsService {

    private final UserFinanceSettingsRepository settingsRepository;
    private final CurrentUserService currentUserService;

    public UserFinanceSettings getCurrentSettings() {
        Long userId = currentUserService.getCurrentUserId();
        return settingsRepository.findByUserId(userId).orElseGet(() -> {
            UserFinanceSettings settings = new UserFinanceSettings(currentUserService.getCurrentUser(),
                    "50/30/20", 5, 80);
            return settings;
        });
    }

    public boolean hasCurrentSettings() {
        return settingsRepository.existsByUserId(currentUserService.getCurrentUserId());
    }

    @Transactional
    public void updateCurrentSettings(FinanceSettingsRequest request) {
        UserFinanceSettings settings = settingsRepository.findByUserId(currentUserService.getCurrentUserId())
                .orElseGet(() -> new UserFinanceSettings(currentUserService.getCurrentUser(), "50/30/20", 5, 80));
        settings.setSalaryDay(request.salaryDay());
        settings.setExpectedSalary(request.expectedSalary());
        settings.setHourlyWage(request.hourlyWage());
        settings.setBudgetReminderTime(request.budgetReminderTime());
        settings.setDailyReminderTime(request.dailyReminderTime());
        settingsRepository.save(settings);
    }
}
