# Phân Chia Nhiệm Vụ Mô-Đun (MODULE_OWNERSHIP.md) - SmartEnvelope

Tài liệu này xác định ranh giới trách nhiệm, danh sách package/file phụ trách của từng thành viên trong nhóm 5 người.

---

## 1. Phân chia chi tiết theo 5 thành viên

### 👤 Thành viên 1: Quản lý dự án, Tài khoản và Quản trị hệ thống (Admin)
- **Mô tả**: Thiết lập nền tảng kiến trúc, bảo mật người dùng, xác thực và các chức năng quản trị hệ thống.
- **Phạm vi file/package phụ trách**:
  - `config/`: `SecurityConfig.java`, `WebConfig.java`
  - `controller/auth/`: `AuthController.java`
  - `controller/admin/`: `AdminController.java`
  - `dto/auth/`: `LoginRequest.java`, `RegisterRequest.java`
  - `dto/user/`: `UserDTO.java`
  - `entity/`: `User.java`
  - `enums/`: `Role.java`
  - `repository/`: `UserRepository.java`
  - `service/`: `AuthService.java`, `UserService.java`, `AdminService.java`
  - `templates/auth/`: `login.html`, `register.html`
  - `templates/admin/`: `users.html`, `categories.html`
  - `templates/fragments/`: `header.html`, `sidebar.html`, `footer.html`

---

### 👤 Thành viên 2: Tài khoản thanh toán và Phong bì ngân sách
- **Mô tả**: Quản lý các nguồn tiền (tiền mặt, ngân hàng, ví điện tử) và mô hình phân bổ phong bì chi tiêu theo chu kỳ.
- **Phạm vi file/package phụ trách**:
  - `controller/account/`: `AccountController.java`
  - `controller/envelope/`: `EnvelopeController.java`
  - `dto/account/`: `AccountRequest.java`, `AccountResponse.java`
  - `dto/envelope/`: `EnvelopeRequest.java`, `EnvelopeResponse.java`
  - `entity/`: `Account.java`, `Envelope.java`, `BudgetPeriod.java`
  - `enums/`: `AccountType.java`
  - `repository/`: `AccountRepository.java`, `EnvelopeRepository.java`, `BudgetPeriodRepository.java`
  - `service/`: `AccountService.java`, `EnvelopeService.java`
  - `templates/account/`: `list.html`, `form.html`
  - `templates/envelope/`: `list.html`, `form.html`

---

### 👤 Thành viên 3: Giao dịch thu, chi và chuyển tiền
- **Mô tả**: Ghi nhận các giao dịch tài chính phát sinh, trừ tiền phong bì và tài khoản tương ứng, quản lý danh mục chi tiêu.
- **Phạm vi file/package phụ trách**:
  - `controller/transaction/`: `TransactionController.java`
  - `dto/transaction/`: `TransactionRequest.java`, `TransactionResponse.java`
  - `entity/`: `Transaction.java`, `Category.java`
  - `enums/`: `TransactionType.java`, `CategoryType.java`
  - `repository/`: `TransactionRepository.java`, `CategoryRepository.java`
  - `service/`: `TransactionService.java`
  - `templates/transaction/`: `list.html`, `form.html`

---

### 👤 Thành viên 4: Ngân sách, Cảnh báo và Tiết kiệm
- **Mô tả**: Thiết lập ngân sách tổng thể, quy tắc phân bổ, hệ thống thông báo/cảnh báo vượt ngân sách và quản lý mục tiêu tiết kiệm, nuôi heo đất.
- **Phạm vi file/package phụ trách**:
  - `controller/budget/`: `BudgetController.java`
  - `controller/alert/`: `AlertController.java`
  - `controller/savings/`: `SavingsGoalController.java`
  - `dto/budget/`: `BudgetRequest.java`, `BudgetResponse.java`
  - `dto/savings/`: `SavingsGoalRequest.java`, `SavingsGoalResponse.java`
  - `entity/`: `Budget.java`, `Alert.java`, `SavingsGoal.java`, `PiggyBank.java`
  - `enums/`: `AlertType.java`
  - `repository/`: `BudgetRepository.java`, `AlertRepository.java`, `SavingsGoalRepository.java`
  - `service/`: `BudgetService.java`, `AlertService.java`, `SavingsGoalService.java`
  - `templates/budget/`: `list.html`, `form.html`
  - `templates/alert/`: `list.html`
  - `templates/savings/`: `list.html`, `form.html`

---

### 👤 Thành viên 5: Dashboard, Báo cáo thống kê và AI
- **Mô tả**: Xây dựng trang tổng quan tài chính, biểu đồ phân tích số liệu và tích hợp gợi ý/phân tích chi tiêu thông minh bằng AI.
- **Phạm vi file/package phụ trách**:
  - `controller/dashboard/`: `DashboardController.java`
  - `controller/report/`: `ReportController.java`
  - `controller/ai/`: `AIController.java`
  - `dto/report/`: `ReportFilterRequest.java`, `ReportSummaryResponse.java`
  - `dto/ai/`: `AIParseRequest.java`, `AIParseResponse.java`
  - `service/`: `DashboardService.java`, `ReportService.java`, `AIService.java`
  - `templates/dashboard/`: `index.html`
  - `templates/report/`: `index.html`

---

## 2. Quy định các tệp dùng chung (Shared Resources)

Các file sau đây phục vụ toàn hệ thống, **bắt buộc phải trao đổi và thống nhất với cả nhóm trước khi chỉnh sửa**:

1. `SmartEnvelopeApplication.java`: Lớp khởi chạy ứng dụng.
2. `pom.xml`: Khai báo thư viện phụ thuộc toàn dự án.
3. `application.properties`: Cấu hình hệ thống (Port, Database, JPA, Thymeleaf).
4. `config/SecurityConfig.java`: Phân quyền bảo mật chung.
5. `exception/GlobalExceptionHandler.java`: Xử lý ngoại lệ toàn cục.
6. `static/css/style.css` & `static/js/app.js`: Tài nguyên giao diện chung.
7. `templates/fragments/`: Header, sidebar, footer của layout chung.
