# SmartEnvelope – Website Quản Lý Ngân Sách Và Chi Tiêu Thông Minh

> Dự án đồ án / bài tập lớn phát triển ứng dụng web quản lý tài chính cá nhân theo phương pháp phân bổ phong bì ngân sách (Envelope Budgeting Method) bằng **Java Spring Boot**, **Thymeleaf** và **MySQL**.

---

## 1. Giới thiệu SmartEnvelope

**SmartEnvelope** là giải pháp phần mềm hỗ trợ người dùng kiểm soát tài chính cá nhân hiệu quả:
- Phân bổ thu nhập vào các phong bì chi tiêu theo chu kỳ (Ăn uống, Nhà ở, Đi lại, Giải trí, Tiết kiệm...).
- Trừ tiền tự động từ phong bì khi phát sinh giao dịch chi tiêu.
- Cảnh báo người dùng khi một phong bì sắp vượt hạn mức chi tiêu cho phép.
- Hỗ trợ lập kế hoạch theo các quy tắc tài chính nổi tiếng (50/30/20).
- Báo cáo trực quan và hỗ trợ phân tích tài chính thông minh bằng AI.

Hệ thống được thiết kế theo kiến trúc chuẩn **Spring MVC đa tầng**, tối ưu cho nhóm 5 sinh viên/lập trình viên phối hợp phát triển song song.

---

## 2. Công nghệ sử dụng

- **Ngôn ngữ**: Java 17 LTS trở lên.
- **Framework**: Spring Boot 3.3.x.
- **Kiến trúc**: Spring MVC.
- **Giao diện (Frontend)**: Thymeleaf, HTML5, CSS3, JavaScript, Bootstrap 5.3, Bootstrap Icons.
- **ORM / Truy vấn**: Spring Data JPA / Hibernate.
- **Cơ sở dữ liệu**: MySQL 8.0+.
- **Bảo mật**: Spring Security 6 (BCrypt Password Encoder).
- **Ràng buộc dữ liệu**: Jakarta Bean Validation.
- **Quản lý dự án / Thư viện**: Apache Maven.

---

## 3. Yêu cầu cài đặt

Trước khi bắt đầu, đảm bảo máy tính đã cài đặt:
1. **JDK 17** trở lên (`java -version`).
2. **Apache Maven 3.8+** (`mvn -v`) hoặc sử dụng Maven có sẵn trong IDE.
3. **MySQL Server 8.0+** đang chạy trên máy cục bộ (cổng mặc định 3306).
4. **Git** (`git --version`).
5. **IDE đề xuất**: IntelliJ IDEA, Visual Studio Code hoặc Eclipse.

---

## 4. Cách tạo Database

Mở MySQL Workbench, phpMyAdmin hoặc terminal MySQL và chạy:

```sql
CREATE DATABASE IF NOT EXISTS smart_envelope
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

---

## 5. Cách cấu hình MySQL

Dự án sử dụng cơ chế đọc biến môi trường để bảo mật thông tin đăng nhập, không commit mật khẩu thật lên Git:

1. Sao chép file `.env.example` thành file `.env` (nếu chạy với công cụ hỗ trợ), hoặc:
2. Thiết lập biến môi trường trực tiếp trong Run Configuration của IDE hoặc terminal:
   - `DB_URL`: `jdbc:mysql://localhost:3306/smart_envelope?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&characterEncoding=UTF-8`
   - `DB_USERNAME`: tên đăng nhập MySQL (ví dụ `root`)
   - `DB_PASSWORD`: mật khẩu MySQL trên máy của bạn
   - `PORT`: 8080 (hoặc cổng khác nếu 8080 đang bận)

Mặc định nếu không truyền biến môi trường, hệ thống sẽ sử dụng:
- URL: `jdbc:mysql://localhost:3306/smart_envelope`
- Username: `root`
- Password: (chuỗi rỗng)

---

## 6. Cách chạy dự án

### Cách 1: Sử dụng Maven dòng lệnh
```bash
# Biên dịch dự án
mvn clean compile

# Chạy ứng dụng
mvn spring-boot:run

# Hoặc truyền biến môi trường trực tiếp qua dòng lệnh:
mvn spring-boot:run -Dspring-boot.run.arguments="--DB_USERNAME=root --DB_PASSWORD=your_password"
```

### Cách 2: Sử dụng IDE (IntelliJ IDEA / Eclipse / VS Code)
1. Mở thư mục dự án `Smartenvelopes2` trong IDE.
2. IDE sẽ tự động nhận diện `pom.xml` và tải các dependencies.
3. Mở file `src/main/java/com/smartenvelope/SmartEnvelopeApplication.java`.
4. Nhấn chuột phải chọn **Run 'SmartEnvelopeApplication'**.
5. Mở trình duyệt và truy cập: `http://localhost:8080`.

---

## 7. Cấu trúc thư mục dự án

```text
SmartEnvelope/
├── .gitignore                          # Cấu hình bỏ qua file rác khi commit Git
├── .env.example                        # Mẫu cấu hình biến môi trường kết nối CSDL
├── README.md                           # Tài liệu giới thiệu và hướng dẫn tổng quan
├── pom.xml                             # Tệp cấu hình dependencies và build Maven
│
├── docs/                               # Thư mục tài liệu kỹ thuật
│   ├── ARCHITECTURE.md                 # Kiến trúc MVC và nhiệm vụ từng package
│   ├── MODULE_OWNERSHIP.md             # Phân chia phạm vi trách nhiệm cho 5 thành viên
│   ├── DATABASE_DESIGN.md              # Khung thiết kế CSDL, bảng và ERD
│   └── GIT_WORKFLOW.md                 # Quy trình làm việc nhóm với Git & PR
│
└── src/
    ├── main/
    │   ├── java/com/smartenvelope/
    │   │   ├── SmartEnvelopeApplication.java  # Class khởi chạy Spring Boot
    │   │   ├── config/                        # Cấu hình Security & Web MVC
    │   │   ├── controller/                    # Controller tiếp nhận request (phân theo 5 TV)
    │   │   ├── dto/                           # Data Transfer Objects
    │   │   ├── entity/                        # Các thực thể JPA ánh xạ CSDL
    │   │   ├── enums/                         # Các enum hệ thống
    │   │   ├── exception/                     # Xử lý ngoại lệ toàn cục
    │   │   ├── repository/                    # Giao diện Spring Data JPA
    │   │   └── service/                       # Tầng xử lý nghiệp vụ
    │   │
    │   └── resources/
    │       ├── application.properties         # Cấu hình ứng dụng Spring Boot
    │       ├── static/                        # CSS, JS, hình ảnh tĩnh
    │       └── templates/                     # Giao diện HTML Thymeleaf
    │
    └── test/                                  # Kiểm thử tự động (Unit Test / Integration Test)
```

---

## 8. Quy tắc làm việc nhóm

1. **Phân chia độc lập**: Mỗi thành viên chỉ phát triển trong các file và package được phân công trong [MODULE_OWNERSHIP.md](docs/MODULE_OWNERSHIP.md).
2. **Quy tắc tiền tệ**: Mọi trường dữ liệu số tiền bắt buộc dùng `java.math.BigDecimal`, tuyệt đối không dùng `float` hay `double`.
3. **Phân tầng chuẩn MVC**: Controller chỉ điều hướng và validate form; Service xử lý toàn bộ nghiệp vụ; Repository chịu trách nhiệm truy vấn database.
4. **Bảo vệ tài nguyên dùng chung**: Không tự ý sửa `pom.xml`, `application.properties`, `SecurityConfig.java`, `GlobalExceptionHandler.java` mà không thông qua cả nhóm.

---

## 9. Quy trình tạo Branch và Pull Request

- Luôn cập nhật mã nguồn mới nhất từ `develop` trước khi code:
  ```bash
  git checkout develop
  git pull origin develop
  git checkout -b feature/memberX-ten-chuc-nang
  ```
- Commit có ý nghĩa: `feat(...)`, `fix(...)`, `docs(...)`.
- Tạo Pull Request hướng tới nhánh **`develop`** (không tạo vào `main`).
- Ít nhất 1 thành viên review và approve trước khi merge.
