# SmartEnvelope

SmartEnvelope là ứng dụng web quản lý tài chính cá nhân và quỹ chung theo phương pháp ngân sách phong bì. Người dùng ghi nhận thu/chi, phân bổ ngân sách theo kỳ, theo dõi số dư phong bì, đặt mục tiêu tiết kiệm và xem báo cáo.

## Công nghệ

- Java 17, Spring Boot 4.1.2, Maven
- Spring MVC, Thymeleaf, Spring Security
- Spring Data JPA, Hibernate, MySQL 8+
- Flyway quản lý cấu trúc dữ liệu và migration
- Bootstrap, CSS và JavaScript
- AI và email là tính năng tùy chọn, cần cấu hình API/SMTP riêng

## Yêu cầu

- JDK 17
- MySQL Server 8 trở lên
- Maven 3.9+ trong `PATH`, hoặc Maven tích hợp trong IDE
- Trình duyệt web

Kiểm tra cài đặt bằng `java -version`, `mvn -version` và kết nối tới MySQL.

## Cài đặt và chạy lần đầu

### 1. Tạo database

Trong MySQL Workbench hoặc MySQL CLI, chạy:

```sql
CREATE DATABASE IF NOT EXISTS smart_envelope
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

Chỉ tạo database rỗng. Khi chạy ứng dụng, Flyway tự tạo/cập nhật bảng từ `src/main/resources/db/migration/`.

### 2. Tạo cấu hình máy cá nhân

Tại thư mục gốc dự án, sao chép `.env.example` thành `.env` rồi sửa thông tin MySQL:

```dotenv
DB_HOST=localhost
DB_PORT=3306
DB_NAME=smart_envelope
DB_USERNAME=root
DB_PASSWORD=mat_khau_mysql_cua_ban
```

`.env` đã nằm trong `.gitignore`; không commit hoặc gửi file này lên repo. Chỉ commit `.env.example` với các giá trị mẫu.

### 3. Khởi chạy ứng dụng

**Windows PowerShell (khuyến nghị):**

```powershell
./run.ps1
```

**Windows Command Prompt:**

```bat
run.bat
```

Hai script trên nạp biến trong `.env`, sau đó chạy `mvn spring-boot:run`. Có thể chạy trực tiếp `SmartEnvelopeApplication` trong IDE; cấu hình VS Code hiện tại dùng `.env` qua `.vscode/launch.json`.

Lệnh `mvn spring-boot:run` chạy trực tiếp trong terminal **không tự đọc file `.env`**. Nếu dùng lệnh này, hãy khai báo biến môi trường trong terminal/Run Configuration trước.

Khi log báo ứng dụng đã khởi động, mở [http://localhost:8080](http://localhost:8080), tạo tài khoản và đăng nhập. Nếu cổng 8080 đang bận, đổi `PORT` trong `.env`.

## Cấu hình tùy chọn

### Xác minh email và email thông báo

Đăng ký, đổi email tài khoản và xác minh email nhận thông báo cần gửi mã qua SMTP. Để dùng Gmail, điền App Password vào `SMTP_PASSWORD` trong `.env`:

```dotenv
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=dia_chi_gui@gmail.com
SMTP_PASSWORD=app_password
SMTP_FROM=dia_chi_gui@gmail.com
SMTP_AUTH=true
SMTP_STARTTLS=true
```

`SMTP_USERNAME` là tài khoản gửi; người dùng nhận mã có thể nhập địa chỉ Gmail của họ trong ứng dụng. Nếu SMTP chưa cấu hình hoặc đăng nhập SMTP thất bại, mã xác minh sẽ không gửi được.

### Tính năng AI

AI không bắt buộc để chạy các nghiệp vụ tài chính thông thường. Muốn bật tính năng phân tích văn bản/hóa đơn, điền `AI_API_KEY`, `AI_API_URL` và `AI_MODEL` theo nhà cung cấp trong `.env`. Không ghi API key trực tiếp vào mã nguồn hay commit lên Git.

## Cấu trúc mã nguồn

```text
src/main/java/com/smartenvelope/
├── config/       cấu hình Spring Security và web
├── controller/   nhận request và điều hướng trang
├── dto/          dữ liệu form và dữ liệu hiển thị
├── entity/       entity JPA ánh xạ bảng MySQL
├── enums/        trạng thái và loại nghiệp vụ
├── exception/    xử lý lỗi
├── repository/   truy vấn dữ liệu
└── service/      nghiệp vụ ứng dụng

src/main/resources/
├── db/migration/ migration Flyway
├── static/       CSS, JavaScript, hình ảnh
└── templates/    giao diện Thymeleaf
```

## Tài liệu dự án

- [Phân công thành viên và lộ trình đọc code](docs/PHAN_CONG.md)
- [Kiến trúc và luồng xử lý](docs/ARCHITECTURE.md)
- [Database và migration](docs/DATABASE.md)
- [Quy ước giao diện](docs/UI_GUIDE.md)
- [Quy trình Git nhóm](docs/GIT_WORKFLOW.md)

## Xử lý lỗi thường gặp

- **Không kết nối được MySQL:** kiểm tra MySQL đang chạy, database `smart_envelope` đã tồn tại và thông tin `DB_*` trong `.env` đúng.
- **`Access denied`:** kiểm tra `DB_USERNAME`/`DB_PASSWORD` và khởi động lại ứng dụng sau khi sửa `.env`.
- **Cổng đã được sử dụng:** đổi `PORT` hoặc dừng tiến trình đang dùng cổng đó.
- **Flyway/Hibernate báo thiếu bảng hoặc sai schema:** không tự sửa bảng thủ công; xem log migration và xác nhận app đang kết nối đúng database.
- **Không gửi được mã email:** kiểm tra SMTP/App Password và log của ứng dụng.
- **AI không phân tích được:** xác nhận API key, URL và model khớp với nhà cung cấp; ứng dụng vẫn có thể chạy các phần không dùng AI.
