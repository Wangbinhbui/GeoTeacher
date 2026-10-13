# Bối cảnh dự án GeoTeacher

## 1. Tầm nhìn

`CONFIRMED` GeoTeacher là trợ lý công việc cho một giáo viên Địa lý dạy trực tiếp tại trường: quản lý lớp/học sinh, xây ngân hàng câu hỏi, tạo và in đề, chấm bài làm trên giấy, quản lý điểm và về sau hỗ trợ tài liệu/công cụ Địa lý/AI.

## 2. Phạm vi MVP

`CONFIRMED`

- Một tài khoản giáo viên; không public registration, không có tài khoản học sinh hay admin riêng.
- M01: đăng nhập, đăng xuất, đổi/khôi phục mật khẩu, hồ sơ và cấu hình cơ bản.
- M02: năm học, lớp, học sinh, ghi danh, import/export.
- M03: ngân hàng bốn loại câu hỏi: `SINGLE_CHOICE`, `TRUE_FALSE`, `SHORT_ANSWER`, `ESSAY`.
- M04: đề hỗn hợp, ma trận, chốt snapshot, nhiều mã đề, Word/PDF và đáp án.
- M05: đợt kiểm tra, nhập đáp án/điểm thủ công, chấm khách quan, chấm tự luận thủ công, chốt/điều chỉnh điểm có lịch sử, export Excel.
- Học sinh làm bài trên giấy. Giáo viên là người duyệt kết quả cuối cùng.

## 3. Ngoài phạm vi MVP

`CONFIRMED`

- LMS, khóa học/video call/chat/thanh toán/đăng ký khóa học.
- Thi online bắt buộc học sinh đăng nhập.
- Multi-school, multi-teacher, RBAC nhiều vai trò và microservices.

`DEFERRED`

- M06 giáo án/tài liệu; M07 công cụ Địa lý.
- OMR/OpenCV, OCR và AI tạo câu hỏi/chấm tự luận.
- Phân tích học tập nâng cao.

## 4. Nguyên tắc bất biến nghiệp vụ

`CONFIRMED`

- Sửa câu hỏi ngân hàng không làm thay đổi đề đã chốt hay kết quả cũ.
- Mỗi mã đề có mapping thứ tự và đáp án riêng, truy xuất được khi chấm.
- Không tự động chấm khi chưa xác định mã đề.
- Không tự động chốt kết quả nhận dạng không chắc chắn.
- Điểm từng câu không vượt tối đa; chỉ chốt khi các phần bắt buộc hoàn tất.
- Thay đổi điểm đã chốt phải có lịch sử; không xóa cứng kết quả đã chốt.

## 5. Hiện trạng repository (2026-10-13)

- Repository Git đã được khởi tạo và liên kết với `https://github.com/Wangbinhbui/GeoTeacher.git`.
- Bộ tài liệu phân tích/thiết kế được duy trì cùng source code.
- Foundation đã có Spring Boot/PostgreSQL/Flyway, React/TypeScript/Tailwind và Docker Compose.
- Lát cắt M01 đầu tiên đã có bootstrap tài khoản, đăng nhập/đăng xuất bằng session + CSRF và đổi mật khẩu; cơ chế khôi phục mật khẩu vẫn `OPEN`.
- Dashboard hiện là khung điều hướng; các số liệu/module M02–M05 chưa được triển khai.
- Bước code kế tiếp: chốt các câu hỏi P0 còn liên quan và triển khai School year/Class → Student/Enrollment.

## 6. Tiêu chí thành công MVP

`PROPOSED`

- Giáo viên có thể đi từ tạo lớp và câu hỏi đến đề đã chốt, in đề, nhập bài làm, chấm và xuất bảng điểm mà không dùng dữ liệu giả.
- Một câu hỏi bị sửa sau đó không làm đổi bản xem trước/đáp án/điểm của đề cũ.
- Chấm lại cùng một submission trên cùng snapshot cho kết quả xác định.
- Các lỗi ma trận, tổng điểm, đáp án, bài trùng và điều chỉnh điểm được chặn hoặc yêu cầu xác nhận đúng quy tắc.
