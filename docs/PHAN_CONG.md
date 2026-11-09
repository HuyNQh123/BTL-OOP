# Phân công thành viên và lộ trình đọc code

Tài liệu này phản ánh cấu trúc mã nguồn hiện tại. Thành viên có thể đổi theo danh sách thực tế của nhóm; trước khi nộp, thay nhãn TV1–TV5 bằng tên thật.

## Bức tranh chung

Mỗi nghiệp vụ thường đi theo đường đi:

`templates/<module>` → `controller/<module>` → `service` → `repository` → `entity` → MySQL

Khi đọc một luồng, bắt đầu từ URL trong Controller, xem model/form nhận vào, theo tiếp lời gọi Service, truy vấn Repository và các Entity bị cập nhật. Sau đó đọc template để hiểu người dùng gửi dữ liệu gì và kết quả được hiển thị ra sao.

## TV1 — Đăng nhập, người dùng, hồ sơ và không gian quỹ

**Nghiệp vụ:** đăng ký/xác thực, vai trò người dùng, hồ sơ cá nhân/đổi email, tạo và quản lý workspace, thành viên/lời mời, quyền truy cập, chức năng quản trị.

**File cần làm chủ:**

- Controllers: `controller/auth/AuthController.java`, `controller/user/ProfileController.java`, `controller/workspace/WorkspaceController.java`, `WorkspaceContextController.java`, `WorkspaceInvitationController.java`, `controller/admin/AdminController.java`, `AdminUserController.java`.
- Services: `service/AuthService.java`, `RegistrationEmailVerificationService.java`, `UserService.java`, `CurrentUserService.java`, `CustomUserDetailsService.java`, `ProfileService.java`, `ProfileEmailChangeService.java`, `WorkspaceContextService.java`, `WorkspaceService.java`, `WorkspaceInvitationService.java`, `AdminService.java`.
- DTO/entities/repositories: `dto/auth/`, `dto/user/`, `dto/workspace/`, `dto/admin/`; `entity/User.java`, `Workspace.java`, `Membership.java`, `WorkspaceInvitation.java`; `repository/UserRepository.java`, `WorkspaceRepository.java`, `MembershipRepository.java`, `WorkspaceInvitationRepository.java`.
- Giao diện: `templates/auth/`, `templates/profile/`, `templates/workspaces/`, `templates/admin/`.
- Bảo mật và cấu hình: `config/SecurityConfig.java`, `config/NotificationEmailSuccessHandler.java`, `controller/advice/GlobalModelAttributeAdvice.java`.

**Đọc theo thứ tự:** `SecurityConfig` → `AuthController` → `CustomUserDetailsService` → `CurrentUserService` → `UserService` → `WorkspaceContextService` → `WorkspaceService` → trang `workspaces`.

## TV2 — Tài khoản tiền, kỳ ngân sách và phong bì

**Nghiệp vụ:** tài khoản tiền mặt/ngân hàng/ví, tạo kỳ ngân sách, phân bổ tổng thu nhập, xác nhận kỳ, quản lý hạn mức và trạng thái phong bì.

**File cần làm chủ:**

- Controllers: `controller/account/AccountController.java`, `controller/budget/BudgetController.java`, `controller/envelope/EnvelopeController.java`.
- Services: `service/AccountService.java`, `BudgetService.java`, `EnvelopeService.java`.
- DTO/entities/repositories: `dto/account/`, `dto/budget/`, `dto/envelope/`; `entity/Account.java`, `BudgetPeriod.java`, `Envelope.java`; `repository/AccountRepository.java`, `BudgetPeriodRepository.java`, `EnvelopeRepository.java`.
- Giao diện: `templates/account/`, `templates/budget/`, `templates/envelope/`.
- Các luồng xác nhận kỳ và tạo phong bì có liên quan tới `TransactionService`; phối hợp TV3 trước khi đổi quy tắc ảnh hưởng giao dịch.

**Đọc theo thứ tự:** `AccountController` → `AccountService` → `BudgetController` → `BudgetService` → `BudgetPeriod` → `EnvelopeController` → `EnvelopeService` → các form/list tương ứng.

## TV3 — Giao dịch, danh mục và điều chuyển bù

**Nghiệp vụ:** ghi nhận thu/chi, kiểm tra account và phong bì, bù khi thiếu ngân sách, lịch sử điều chuyển, audit giao dịch, danh mục thu/chi.

**File cần làm chủ:**

- Controllers: `controller/transaction/TransactionController.java`, `controller/admin/AdminCategoryController.java`.
- Services: `service/TransactionService.java`, `CategoryService.java`.
- DTO/entities/repositories: `dto/transaction/`, `dto/admin/AdminCategoryRequest.java`; `entity/Transaction.java`, `EnvelopeTransfer.java`, `TransactionAudit.java`, `Category.java`; `repository/TransactionRepository.java`, `EnvelopeTransferRepository.java`, `TransactionAuditRepository.java`, `CategoryRepository.java`.
- Enums: `TransactionType.java`, `TransactionStatus.java`, `CategoryType.java`, `TransferReason.java`.
- Giao diện: `templates/transaction/`, `templates/admin/categories.html`, `category-create.html`, `category-edit.html`.
- Các thao tác sửa hạn mức/đóng kỳ của TV2/TV4 có thể ảnh hưởng sổ giao dịch; không đổi một phía nếu chưa rà soát các Service dùng chung.

**Đọc theo thứ tự:** `TransactionController` → `TransactionRequest` → `TransactionService` → `Transaction` và `EnvelopeTransfer` → repository → `templates/transaction/form.html`.

## TV4 — Cài đặt tài chính, nhắc nhở, cảnh báo và tiết kiệm

**Nghiệp vụ:** lịch nhận lương, mức lương/thu nhập theo giờ, giờ nhắc, thông báo trong app/email, streak, mục tiêu tiết kiệm, heo đất và đóng kỳ.

**File cần làm chủ:**

- Controllers: `controller/settings/FinanceSettingsController.java`, `controller/alert/AlertController.java`, `controller/savings/SavingsGoalController.java`, `controller/account/NotificationEmailController.java`.
- Services: `service/FinanceSettingsService.java`, `FinanceReminderScheduler.java`, `AlertService.java`, `NotificationEmailVerificationService.java`, `BudgetStreakService.java`, `SavingsGoalService.java`, `EmailService.java`.
- DTO/entities/repositories: `dto/settings/FinanceSettingsRequest.java`, `dto/savings/`; `entity/UserFinanceSettings.java`, `Notification.java`, `SavingsGoal.java`, `PiggyBank.java`, `PiggyBankEntry.java`, `MonthClose.java`; `repository/UserFinanceSettingsRepository.java`, `NotificationRepository.java`, `SavingsGoalRepository.java`, `PiggyBankRepository.java`, `PiggyBankEntryRepository.java`, `MonthCloseRepository.java`.
- Giao diện: `templates/settings/finance.html`, `templates/alert/list.html`, `templates/savings/`, `templates/account/notification-email.html`.
- `BudgetService` tham gia tính và xác nhận kỳ; phối hợp TV2. `EmailService` dùng chung với TV1 cho xác minh tài khoản/lời mời.

**Đọc theo thứ tự:** `FinanceSettingsController` → `FinanceSettingsService` → `UserFinanceSettings` → `FinanceReminderScheduler` → `AlertService` → `SavingsGoalService` → các trang settings/alert/savings.

## TV5 — Tổng quan, báo cáo và AI

**Nghiệp vụ:** dashboard theo kỳ/quỹ, báo cáo thu chi và huy hiệu, gợi ý phân bổ từ lịch sử, nhập giao dịch bằng câu tự nhiên và bóc tách ảnh hóa đơn thành bản nháp.

**File cần làm chủ:**

- Controllers: `controller/dashboard/DashboardController.java`, `controller/report/ReportController.java`, `controller/ai/AIController.java`.
- Services: `service/DashboardService.java`, `ReportService.java`, `AIService.java`.
- DTOs: `dto/report/`, `dto/ai/`.
- Giao diện: `templates/dashboard/index.html`, `templates/report/index.html`, phần nhập AI trong `templates/transaction/form.html`.
- Dashboard/báo cáo phụ thuộc dữ liệu và quy tắc do TV2–TV4 cung cấp. AI chỉ tạo bản nháp; phần lưu cuối vẫn do `TransactionService` kiểm tra.

**Đọc theo thứ tự:** `DashboardController` → `DashboardService` → `ReportController` → `ReportService` → `AIController` → `AIService` → `AIParseResponse` và cách form đổ dữ liệu nháp.

## File dùng chung — cần phối hợp trước khi sửa

- `pom.xml`, `application.properties`, `.env.example`, `SmartEnvelopeApplication.java`.
- `config/SecurityConfig.java`, `controller/advice/GlobalModelAttributeAdvice.java`, `exception/GlobalExceptionHandler.java`.
- `entity/Workspace.java`, `Membership.java`, `User.java` và `service/CurrentUserService.java` vì nhiều module dựa vào phân quyền người dùng/quỹ.
- `templates/fragments/`, `templates/layouts/`, `static/css/`, `static/js/`.
- Migration `src/main/resources/db/migration/V*.sql`: không sửa migration đã áp dụng; tạo migration mới và nhờ thành viên liên quan review.

## Cách học và trình bày phần được giao

Mỗi thành viên chọn một luồng thật và có thể giải thích:

1. Người dùng bắt đầu ở trang/URL nào?
2. Form gửi DTO/parameter nào và validation nằm ở đâu?
3. Controller gọi Service nào? Nghiệp vụ nào được kiểm tra ở Service?
4. Repository truy vấn những Entity/bảng nào? Dữ liệu thuộc user/workspace nào?
5. Điều gì xảy ra khi dữ liệu sai hoặc một bước lưu thất bại?
6. Template hiển thị thành công/lỗi ra sao?

Đừng chỉ học thuộc tên file. Hãy lần theo một request hoàn chỉnh, vẽ lại đường đi và giải thích một quy tắc nghiệp vụ bằng ví dụ số cụ thể.
