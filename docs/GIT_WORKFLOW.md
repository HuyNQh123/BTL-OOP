# Quy trình Git nhóm

Tài liệu này là quy trình gợi ý; trước khi dùng, kiểm tra nhánh thực tế của repository bằng `git branch -a`.

## Làm một đầu việc

```bash
git status
git switch develop
git pull
git switch -c feature/memberX-mo-ta-ngan
```

Thay `memberX` bằng mã thành viên và mô tả bằng công việc cụ thể. Tránh để hai người sửa cùng một nhóm file nếu có thể; thay đổi entity, migration, service dùng chung cần trao đổi với thành viên liên quan.

## Commit và chia sẻ

```bash
git add <cac-file-cua-cong-viec>
git commit -m "feat(envelope): add envelope allocation"
git push -u origin feature/memberX-mo-ta-ngan
```

Tạo Pull Request vào nhánh tích hợp mà nhóm đang sử dụng, ghi rõ nghiệp vụ, file quan trọng, cách tự kiểm tra và migration mới (nếu có). Không commit `.env`, mật khẩu, API key hoặc dữ liệu người dùng. Kiểm tra `git status` và nội dung staged trước khi commit.

## Quy tắc cơ bản

- Dùng tiền tố commit dễ hiểu: `feat`, `fix`, `docs`, `refactor`, `chore`.
- Mỗi nhánh nên giải quyết một đầu việc; chia thay đổi lớn thành các PR nhỏ.
- Không sửa migration đã chạy ở môi trường khác; thêm migration phiên bản tiếp theo.
- Khi xử lý xung đột, giữ đúng nghiệp vụ của cả hai thay đổi rồi nhờ chủ sở hữu module review.
- Không dùng `git add .` một cách máy móc; kiểm tra để không đưa bí mật hoặc file sinh tự động vào commit.
