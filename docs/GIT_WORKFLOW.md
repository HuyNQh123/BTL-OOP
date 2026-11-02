# Quy Trình Làm Việc Git (GIT_WORKFLOW.md) - SmartEnvelope

Tài liệu này hướng dẫn quy chuẩn cộng tác mã nguồn trên Git & GitHub dành cho 5 thành viên của dự án SmartEnvelope.

---

## 1. Mô hình phân nhánh (Branching Strategy)

Hệ thống quản lý phiên bản tuân theo mô hình **Git Flow chuẩn**:

- `main`: Nhánh production ổn định tuyệt đối. Chỉ chứa mã nguồn đã kiểm thử và sẵn sàng báo cáo/triển khai. **Không commit trực tiếp lên main.**
- `develop`: Nhánh tích hợp chính của cả nhóm. Mọi tính năng sau khi viết xong sẽ được merge vào đây thông qua Pull Request.
- `feature/<tên_thành_viên>-<chức_năng>`: Nhánh làm việc riêng của từng cá nhân khi thực hiện một nhiệm vụ cụ thể.
  - Ví dụ: `feature/member2-account-crud`
  - Ví dụ: `feature/member3-transaction-logging`
  - Ví dụ: `feature/member4-alert-rules`
  - Ví dụ: `feature/member5-dashboard-charts`

---

## 2. Quy trình làm việc hàng ngày (Daily Workflow)

### Bước 1: Đồng bộ mã nguồn mới nhất từ `develop`
Trước khi bắt đầu code tính năng mới, luôn kéo mã nguồn mới nhất:
```bash
git checkout develop
git pull origin develop
```

### Bước 2: Tạo nhánh tính năng mới từ `develop`
```bash
git checkout -b feature/memberX-feature-name
```

### Bước 3: Phát triển và commit theo quy chuẩn Conventional Commits
Quy cách viết commit message:
- `feat`: Thêm tính năng mới (ví dụ: `feat(account): add create account form`)
- `fix`: Sửa lỗi (ví dụ: `fix(auth): fix password encoding error`)
- `docs`: Cập nhật tài liệu (ví dụ: `docs(database): update ERD table fields`)
- `refactor`: Tái cấu trúc mã nguồn không làm thay đổi tính năng
- `style`: Định dạng code, CSS giao diện

Ví dụ thực hiện:
```bash
git add .
git commit -m "feat(envelope): add envelope model and skeleton controller"
```

### Bước 4: Đẩy nhánh lên Remote Repository
```bash
git push -u origin feature/memberX-feature-name
```

### Bước 5: Tạo Pull Request (PR) trên GitHub
- Đặt tiêu đề PR rõ ràng: `[Feature] Module Account - Quản lý tài khoản thanh toán`
- Mô tả nội dung đã làm, các file đã thay đổi.
- Chọn Base branch là: `develop` (TUYỆT ĐỐI KHÔNG chọn `main`).
- Yêu cầu ít nhất 1 thành viên khác trong nhóm review và duyệt trước khi Merge.

### Bước 6: Xử lý xung đột (Merge Conflict) nếu có
Nếu nhánh `develop` đã có thay đổi mới trong lúc bạn đang code:
```bash
git checkout develop
git pull origin develop
git checkout feature/memberX-feature-name
git merge develop
# Mở IDE giải quyết các xung đột (nếu có), sau đó:
git add .
git commit -m "chore: resolve merge conflicts with develop"
git push origin feature/memberX-feature-name
```
