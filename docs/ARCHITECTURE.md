# Kiến Trúc Hệ Thống (ARCHITECTURE.md) - SmartEnvelope

Tài liệu này mô tả kiến trúc tổng thể, mô hình phân tầng Spring MVC và nhiệm vụ chi tiết của từng package trong dự án SmartEnvelope.

---

## 1. Tổng quan Kiến trúc

Dự án áp dụng mô hình kiến trúc **Spring Boot MVC đa tầng (Layered Architecture)**, kết hợp giao diện Thymeleaf (Server-Side Rendering).

```text
       [Client Browser]
              │
              ▼  HTTP Request (GET/POST)
       [Controller Layer]  <───> [DTO Layer] (Data Transfer)
              │
              ▼
        [Service Layer]    (Xử lý nghiệp vụ, kiểm tra ràng buộc)
              │
              ▼
      [Repository Layer]   (Spring Data JPA / Hibernate)
              │
              ▼
        [Entity Layer]     (Ánh xạ bảng CSDL)
              │
              ▼
      [MySQL Database]     (Hệ quản trị CSDL)
```

---

## 2. Nhiệm vụ chi tiết của từng Package

### 2.1. `com.smartenvelope.config`
- Chứa các class cấu hình hệ thống:
  - `SecurityConfig.java`: Cấu hình Spring Security, phân quyền URL, xác thực đăng nhập, mã hóa mật khẩu (`BCryptPasswordEncoder`).
  - `WebConfig.java`: Cấu hình Spring MVC, đăng ký view controller, cấu hình đường dẫn tài nguyên tĩnh, định dạng ngày tháng.

### 2.2. `com.smartenvelope.controller`
- Tiếp nhận HTTP requests từ client, validate dữ liệu đầu vào qua DTO, gọi tầng Service để xử lý và trả về view Thymeleaf tương ứng hoặc chuyển hướng (redirect).
- Được phân chia theo từng module nghiệp vụ:
  - `auth/`: Đăng ký, đăng nhập, đăng xuất (Thành viên 1).
  - `admin/`: Quản trị người dùng, danh mục dùng chung (Thành viên 1).
  - `account/`: Quản lý tài khoản thanh toán, nguồn tiền (Thành viên 2).
  - `envelope/`: Quản lý các phong bì ngân sách (Thành viên 2).
  - `transaction/`: Ghi chép thu chi, chuyển khoản (Thành viên 3).
  - `budget/`: Kỳ ngân sách và phân bổ ngân sách (Thành viên 4).
  - `alert/`: Thông báo và cảnh báo chi tiêu (Thành viên 4).
  - `savings/`: Mục tiêu tiết kiệm và heo đất (Thành viên 4).
  - `dashboard/`: Trang tổng quan tài chính cá nhân (Thành viên 5).
  - `report/`: Báo cáo phân tích và thống kê (Thành viên 5).
  - `ai/`: Phân tích và gợi ý tài chính thông minh (Thành viên 5).

### 2.3. `com.smartenvelope.dto`
- Chứa các Data Transfer Object (DTO) dùng để nhận dữ liệu từ form giao diện hoặc trả dữ liệu cho view:
  - `Request`: Nhận dữ liệu người dùng gửi lên kèm annotation validation (`@NotBlank`, `@DecimalMin`, v.v.).
  - `Response` / `DTO`: Chuẩn bị dữ liệu hiển thị trên view, tránh để lộ trực tiếp toàn bộ thuộc tính của Entity.

### 2.4. `com.smartenvelope.entity`
- Chứa các lớp thực thể JPA ánh xạ tới các bảng trong cơ sở dữ liệu MySQL (`@Entity`, `@Table`, `@Id`, `@Column`, `@ManyToOne`, v.v.).

### 2.5. `com.smartenvelope.enums`
- Định nghĩa các hằng số phân loại bất biến trong hệ thống (`Role`, `AccountType`, `TransactionType`, `CategoryType`, `AlertType`).

### 2.6. `com.smartenvelope.repository`
- Chứa các interface kế thừa `JpaRepository` để thực hiện các thao tác CRUD và truy vấn dữ liệu với MySQL.

### 2.7. `com.smartenvelope.service`
- Chứa các lớp xử lý nghiệp vụ chính, tính toán số dư, kiểm tra hạn mức ngân sách, thực hiện giao dịch an toàn (`@Transactional`).

### 2.8. `com.smartenvelope.exception`
- Quản lý các ngoại lệ nghiệp vụ và xử lý lỗi tập trung toàn cục (`@ControllerAdvice`).

### 2.9. `src/main/resources`
- `application.properties`: Cấu hình datasource, JPA, Thymeleaf, server port.
- `static/`: Chứa file tĩnh dùng chung (`css/`, `js/`, `images/`).
- `templates/`: Chứa các template HTML Thymeleaf, được tổ chức theo module tương ứng với controller.
