# Thiết Kế Cơ Sở Dữ Liệu (DATABASE_DESIGN.md) - SmartEnvelope

Tài liệu này là khung thiết kế cơ sở dữ liệu (Database Design & ERD) để cả 5 thành viên thảo luận, chuẩn hóa và bổ sung chi tiết các bảng, cột và ràng buộc dữ liệu.

---

## 1. Thông tin CSDL chung

- **Hệ quản trị**: MySQL 8.0+
- **Tên Database**: `smart_envelope`
- **Mã hóa (Charset)**: `utf8mb4`
- **Đối chiếu (Collation)**: `utf8mb4_unicode_ci`
- **Quy tắc tiền tệ**: Sử dụng `DECIMAL(15, 2)` trong CSDL và `java.math.BigDecimal` trong mã nguồn Java.

---

## 2. Sơ đồ Quan hệ Thực thể (ERD Overview)

```text
[users] (Thành viên 1)
   │
   ├── 1:N ── [accounts] (Thành viên 2)
   │
   ├── 1:N ── [budget_periods] (Thành viên 2)
   │                 │
   │                 └── 1:N ── [envelopes] (Thành viên 2)
   │                                 │
   │                                 ├── 1:1 ── [categories] (Thành viên 3)
   │                                 │
   │                                 └── 1:N ── [transactions] (Thành viên 3)
   │
   ├── 1:N ── [budgets] (Thành viên 4)
   │
   ├── 1:N ── [alerts] (Thành viên 4)
   │
   └── 1:N ── [savings_goals] (Thành viên 4)
                     │
                     └── 1:1 ── [piggy_banks] (Thành viên 4)
```

---

## 3. Khung chi tiết các bảng CSDL

### 3.1. Bảng `users` (Thành viên 1)
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `full_name` (VARCHAR(100), NOT NULL)
- `email` (VARCHAR(100), NOT NULL, UNIQUE)
- `password` (VARCHAR(255), NOT NULL)
- `role` (VARCHAR(20), NOT NULL)
- `enabled` (BOOLEAN, DEFAULT TRUE)
- `locked` (BOOLEAN, DEFAULT FALSE)
- `created_at` (DATETIME)

### 3.2. Bảng `accounts` (Thành viên 2)
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `user_id` (BIGINT, FK -> users.id)
- `name` (VARCHAR(100), NOT NULL)
- `type` (VARCHAR(30), NOT NULL: CASH, BANK, E_WALLET)
- `balance` (DECIMAL(15, 2), NOT NULL, DEFAULT 0.00)
- `created_at` (DATETIME)

### 3.3. Bảng `budget_periods` (Thành viên 2)
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `user_id` (BIGINT, FK -> users.id)
- `name` (VARCHAR(100), NOT NULL)
- `start_date` (DATE, NOT NULL)
- `end_date` (DATE, NOT NULL)
- `total_income` (DECIMAL(15, 2))

### 3.4. Bảng `envelopes` (Thành viên 2)
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `budget_period_id` (BIGINT, FK -> budget_periods.id)
- `category_id` (BIGINT, FK -> categories.id)
- `name` (VARCHAR(100), NOT NULL)
- `allocated_amount` (DECIMAL(15, 2), NOT NULL)
- `spent_amount` (DECIMAL(15, 2), NOT NULL, DEFAULT 0.00)

### 3.5. Bảng `categories` (Thành viên 3)
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `name` (VARCHAR(100), NOT NULL)
- `type` (VARCHAR(20), NOT NULL: EXPENSE, INCOME)
- `icon` (VARCHAR(50))
- `is_default` (BOOLEAN, DEFAULT TRUE)
- `active` (BOOLEAN, DEFAULT TRUE)

### 3.6. Bảng `transactions` (Thành viên 3)
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `user_id` (BIGINT, FK -> users.id)
- `account_id` (BIGINT, FK -> accounts.id)
- `envelope_id` (BIGINT, FK -> envelopes.id, NULLABLE)
- `category_id` (BIGINT, FK -> categories.id)
- `amount` (DECIMAL(15, 2), NOT NULL)
- `type` (VARCHAR(20), NOT NULL: INCOME, EXPENSE, TRANSFER)
- `transaction_date` (DATETIME, NOT NULL)
- `note` (TEXT)

### 3.7. Bảng `budgets` (Thành viên 4)
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `user_id` (BIGINT, FK -> users.id)
- `name` (VARCHAR(100), NOT NULL)
- `total_budget` (DECIMAL(15, 2), NOT NULL)
- `rules` (VARCHAR(50): E.g., RULE_50_30_20)

### 3.8. Bảng `alerts` (Thành viên 4)
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `user_id` (BIGINT, FK -> users.id)
- `type` (VARCHAR(30), NOT NULL: OVERSPENT, NEAR_LIMIT, REMINDER)
- `message` (TEXT, NOT NULL)
- `is_read` (BOOLEAN, DEFAULT FALSE)
- `created_at` (DATETIME)

### 3.9. Bảng `savings_goals` & `piggy_banks` (Thành viên 4)
- `savings_goals`: `id`, `user_id`, `name`, `target_amount`, `current_amount`, `deadline`
- `piggy_banks`: `id`, `savings_goal_id`, `accumulated_amount`, `last_deposit_date`

---

## 4. Kế hoạch hoàn thiện
- [ ] Nhóm họp thống nhất quan hệ Foreign Keys và On Delete (Cascade / Set Null).
- [ ] Bổ sung script SQL DDL chi tiết vào thư mục này khi hoàn thiện.

