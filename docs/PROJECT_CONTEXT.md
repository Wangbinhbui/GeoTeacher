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

## 2.1. Phạm vi AI đã xác nhận cho giai đoạn mở rộng

- Tạo bản nháp đề kiểm tra theo preset thời lượng 15, 45 hoặc 60 phút.
- Đọc tài liệu/giáo trình do giáo viên tải lên và dùng nội dung truy xuất được làm nguồn tạo câu hỏi/đề.
- Mỗi nội dung AI đề xuất phải cho phép truy vết nguồn và luôn qua bước giáo viên duyệt trước khi đưa vào ngân hàng hoặc finalize đề.
- Nhà cung cấp AI, retention/quyền riêng tư, preset ma trận chi tiết và OCR tài liệu scan vẫn là `OPEN`.

## 3. Ngoài phạm vi MVP

`CONFIRMED`

- LMS, khóa học/video call/chat/thanh toán/đăng ký khóa học.
- Thi online bắt buộc học sinh đăng nhập.
- Multi-school, multi-teacher, RBAC nhiều vai trò và microservices.

`DEFERRED`

- M06 giáo án/tài liệu; M07 công cụ Địa lý.
- OMR/OpenCV, OCR và AI chấm tự luận. AI tạo đề bám tài liệu đã được xác nhận nhưng triển khai sau nền question/exam/document.
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
- Dashboard đã nối tới màn hình quản lý năm học/lớp và học sinh; M02 có migration, API, UI cho School year/Class/Student/Enrollment, archive, rời lớp và optimistic conflict.
- O-002/O-003/O-004 đã chốt: mã học sinh duy nhất toàn hệ thống, được phép thuộc nhiều lớp cùng năm và import dùng preview + all-or-nothing. Bước code kế tiếp của M02 là import/export Excel.
- AI tạo đề bám tài liệu đã vào roadmap mở rộng, chưa chen ngang dependency M02–M04.

## 6. Tiêu chí thành công MVP

`PROPOSED`

- Giáo viên có thể đi từ tạo lớp và câu hỏi đến đề đã chốt, in đề, nhập bài làm, chấm và xuất bảng điểm mà không dùng dữ liệu giả.
- Một câu hỏi bị sửa sau đó không làm đổi bản xem trước/đáp án/điểm của đề cũ.
- Chấm lại cùng một submission trên cùng snapshot cho kết quả xác định.
- Các lỗi ma trận, tổng điểm, đáp án, bài trùng và điều chỉnh điểm được chặn hoặc yêu cầu xác nhận đúng quy tắc.
