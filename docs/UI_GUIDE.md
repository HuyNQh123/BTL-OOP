# Hướng dẫn giao diện

SmartEnvelope dùng giao diện web Thymeleaf với thanh điều hướng trái, thanh tiêu đề trên cùng và vùng nội dung theo trang. Khi bổ sung chức năng, giữ nguyên ngôn ngữ hình ảnh hiện tại.

## Tệp cần xem

- Trang: `src/main/resources/templates/`
- CSS/JavaScript/tài nguyên: `src/main/resources/static/`
- Bố cục dùng chung: tìm fragment/layout trong `templates/` trước khi thêm header hoặc sidebar mới.
- Controller tương ứng: `src/main/java/com/smartenvelope/controller/`

## Quy tắc nhất quán

- Dùng tiếng Việt cho nhãn và thông báo hướng tới người dùng.
- Tái sử dụng card, form, bảng, nút và màu đang có; tránh tạo kiểu riêng nếu thành phần hiện tại đáp ứng được.
- Form cần nhãn rõ, thông báo lỗi đặt gần đầu nội dung hoặc trường liên quan, và có đường quay lại/hủy phù hợp.
- Giữ hiển thị số tiền theo định dạng Việt Nam và ghi rõ đơn vị đồng.
- Bố cục phải co giãn ở màn hình nhỏ; bảng rộng cần có cách cuộn ngang.
- Không để lỗi kỹ thuật, stack trace hoặc thông tin bí mật xuất hiện trong trang.

## Tài liệu gộp

Tệp này gộp các ghi chú thiết kế và bố cục trước đây. Khi thiết kế thay đổi, cập nhật tài liệu này thay vì tạo thêm tài liệu cùng nội dung.
