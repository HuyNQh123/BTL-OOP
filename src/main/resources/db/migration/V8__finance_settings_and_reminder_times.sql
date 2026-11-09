ALTER TABLE user_finance_settings
    ADD COLUMN expected_salary DECIMAL(19, 0) NOT NULL DEFAULT 0 AFTER salary_day,
    ADD COLUMN budget_reminder_time TIME NOT NULL DEFAULT '10:00:00' AFTER expected_salary;

UPDATE user_finance_settings
SET daily_reminder_time = '22:00:00'
WHERE daily_reminder_time IS NULL;

ALTER TABLE user_finance_settings
    MODIFY COLUMN daily_reminder_time TIME NOT NULL DEFAULT '22:00:00';
