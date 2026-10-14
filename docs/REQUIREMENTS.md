# Yêu cầu GeoTeacher

## 1. Actor

- `CONFIRMED` **Giáo viên Địa lý** là actor nghiệp vụ duy nhất trong MVP.
- `DEFERRED` Hệ thống nhận dạng/AI là actor hỗ trợ; không có quyền chốt điểm.

## 2. Yêu cầu chức năng

### FR-AUTH — Xác thực và cấu hình

- `CONFIRMED` Đăng nhập, đăng xuất, đổi mật khẩu, khôi phục mật khẩu; không đăng ký công khai.
- `CONFIRMED` Quản lý hồ sơ giáo viên, tên trường, năm học, mẫu đề và thang điểm mặc định.
- `OPEN` Kênh khôi phục mật khẩu (email hay quy trình one-time token cục bộ) chưa được chọn.

### FR-CLS — Lớp học và học sinh

- `CONFIRMED` Tạo/sửa/lưu trữ lớp theo năm học; dữ liệu năm cũ được giữ lại.
- `CONFIRMED` Thêm/sửa/tìm kiếm học sinh; quản lý ghi danh giữa học sinh và lớp.
- `CONFIRMED` Import/export Excel và xem lịch sử kiểm tra/kết quả theo lớp.
- `CONFIRMED` Mã học sinh duy nhất trên toàn hệ thống; việc thuộc lớp/năm học được quản lý bằng ghi danh.
- `CONFIRMED` Một học sinh có thể đồng thời thuộc nhiều lớp trong cùng năm học; không được ghi danh trùng vào cùng một lớp.
- `CONFIRMED` Import học sinh/ghi danh phải có bước preview và chỉ commit khi toàn bộ dữ liệu hợp lệ (all-or-nothing).
- `CONFIRMED` Mã lớp không được trùng trong cùng một năm học; được phép dùng lại ở năm học khác.

### FR-QST — Ngân hàng câu hỏi

- `CONFIRMED` CRUD, sao chép, xem trước, tìm kiếm/lọc, import Excel và import Word có cấu trúc.
- `CONFIRMED` Lưu khối lớp, chủ đề/bài, mức độ nhận thức, nội dung, đáp án, giải thích, hướng dẫn/rubric, điểm gợi ý và tệp đính kèm.
- `CONFIRMED` `SINGLE_CHOICE` có đúng một đáp án đúng.
- `CONFIRMED` `TRUE_FALSE` có nhiều mệnh đề, mỗi mệnh đề có đáp án Boolean và bảng quy đổi cấu hình được.
- `CONFIRMED` `SHORT_ANSWER` hỗ trợ nhiều đáp án, sai số, đơn vị, làm tròn và chuẩn hóa có quy tắc rõ.
- `CONFIRMED` `ESSAY` có rubric/hướng dẫn, điểm tối đa/từng ý và do giáo viên chấm trong MVP.
- `PROPOSED` Trạng thái câu hỏi: `DRAFT`, `ACTIVE`, `ARCHIVED`; archive thay vì xóa nếu đã được dùng.

### FR-EXM — Đề kiểm tra

- `CONFIRMED` Tạo đề hỗn hợp với các phần bật/tắt, câu hỏi, thứ tự và điểm; tổng điểm mặc định 10 và phải khớp cấu hình.
- `CONFIRMED` Lập ma trận theo chủ đề/bài, loại, mức độ, số câu và số điểm; báo rõ ô thiếu, không tự nới điều kiện.
- `CONFIRMED` Chốt đề tạo snapshot bất biến của nội dung, phương án/mệnh đề, đáp án, rubric, điểm, thứ tự và tài liệu cần thiết.
- `CONFIRMED` Sinh nhiều mã đề, có thể đảo câu/phương án nhưng giữ nhóm ngữ liệu và phần không được đảo; lưu mapping đáp án riêng.
- `CONFIRMED` Đề đã chốt không sửa trực tiếp; sửa phải tạo phiên bản mới.
- `CONFIRMED` Xem trước, sao chép, lưu trữ, xuất Word/PDF và đáp án/hướng dẫn chấm.
- `OPEN` Mã đề do người dùng nhập hay hệ thống sinh; phạm vi duy nhất của mã.
- `OPEN` Có cho phép sinh thêm mã đề sau khi một phiên bản đã được dùng để chấm không.

### FR-GRD — Chấm bài và điểm

- `CONFIRMED` Tạo đợt kiểm tra gắn lớp và đề; mỗi bài gắn học sinh, đợt và đúng mã đề.
- `CONFIRMED` Nhập đáp án thủ công, tự chấm phần khách quan, nhập điểm tự luận/từng câu/từng phần/tổng theo chính sách.
- `CONFIRMED` Phân biệt điểm hệ thống tính với điểm nhập/điều chỉnh thủ công.
- `CONFIRMED` Import điểm Excel có kiểm tra học sinh, mã, trùng, thiếu và giới hạn điểm.
- `CONFIRMED` Duyệt/chốt điểm; không có hai bài chính thức cho cùng học sinh và đợt nếu không qua quy trình thay thế.
- `CONFIRMED` Điều chỉnh điểm đã chốt có audit; không xóa cứng kết quả đã chốt.
- `CONFIRMED` Export kết quả/bảng điểm Excel.
- `OPEN` Chính sách bài vắng, miễn kiểm tra, làm lại và bài thay thế.
- `OPEN` Cho phép nhập thẳng tổng điểm trong trường hợp nào và mức audit/giải trình bắt buộc.

### FR-AI — Tạo đề bám tài liệu

- `CONFIRMED` Giáo viên có thể yêu cầu AI tạo bản nháp đề theo thời lượng 15, 45 hoặc 60 phút.
- `CONFIRMED` Mục đích đề gồm kiểm tra 15 phút, giữa kỳ, cuối kỳ, tổng hợp cả năm và ôn/thi tốt nghiệp THPT; giáo viên chọn riêng thời lượng 15/45/60 phút.
- `CONFIRMED` Giáo viên có thể tải tài liệu/giáo trình để AI dùng làm nguồn kiến thức khi tạo câu hỏi và đề.
- `CONFIRMED` Giáo viên có thể tải một đề mẫu; AI chỉ mô phỏng form/bố cục/phong cách, không sao chép câu hỏi và không coi đề mẫu là nguồn kiến thức.
- `CONFIRMED` AI chỉ đề xuất draft; giáo viên phải xem, sửa và duyệt trước khi đưa vào ngân hàng câu hỏi hoặc finalize đề.
- `CONFIRMED` Nội dung sinh từ tài liệu phải có liên kết nguồn đủ để giáo viên kiểm chứng; không được âm thầm dùng kiến thức ngoài nguồn khi chế độ “chỉ theo giáo trình” được chọn.
- `OPEN` Nhà cung cấp/model, chính sách gửi dữ liệu/retention, preset ma trận cho từng thời lượng và định dạng tài liệu đầu tiên.

> Hiện trạng triển khai 2026-10-14: đã có ingestion PDF/DOCX, phân loại giáo trình/đề mẫu, chọn nguồn, adapter OpenAI Responses API, structured draft có citation và màn hình review. AI mặc định tắt; chỉ gửi các đoạn đã chọn sau consent từng lần. Chưa hỗ trợ OCR hoặc đưa draft vào đề finalized.

## 3. Yêu cầu phi chức năng

### Security và privacy

- `CONFIRMED` Hash mật khẩu bằng thuật toán phù hợp (BCrypt là lựa chọn chấp nhận được), HTTPS khi triển khai.
- `CONFIRMED` Không public đáp án, rubric bí mật hay dữ liệu học sinh.
- `PROPOSED` Dùng session cookie `HttpOnly`, `Secure`, `SameSite` cho ứng dụng same-origin một người dùng; CSRF protection bật cho lệnh thay đổi dữ liệu.
- `PROPOSED` Giới hạn kích thước/loại tệp, tạo tên lưu trữ phía server, checksum và không phục vụ upload như nội dung thực thi.

### Integrity và reliability

- `CONFIRMED` FK, unique constraint, transaction, backend validation, audit và migration.
- `CONFIRMED` Không mất dữ liệu khi import/export hay request thất bại; có backup/restore.
- `PROPOSED` Các thao tác finalize exam, tạo variant, finalize/adjust grade là transaction nguyên tử và idempotent khi có retry.
- `PROPOSED` Optimistic locking cho aggregate dễ bị ghi đồng thời.

### Performance

- `CONFIRMED` Pagination, filtering, index phù hợp, tránh N+1.
- `PROPOSED` Mục tiêu ban đầu: danh sách thông thường phản hồi dưới 2 giây ở quy mô một giáo viên; export/generate lớn chạy job có trạng thái nếu vượt ngưỡng đồng bộ.

### Maintainability và test

- `CONFIRMED` Modular monolith, package-by-feature, DTO thay entity, clean code/SOLID có chọn lọc.
- `CONFIRMED` Unit test cho nghiệp vụ chấm; validation và test đi kèm tính năng.
- `PROPOSED` Test state transition, snapshot immutability, permutation mapping, rounding/tolerance và audit bằng bộ case bảng dữ liệu.

### Export và UX

- `CONFIRMED` Word/PDF/Excel hỗ trợ tiếng Việt; giữ bảng, hình ảnh, biểu đồ và mẫu đề rõ ràng.
- `CONFIRMED` UI hiện đại, dễ dùng, responsive; ưu tiên desktop cho soạn đề và hỗ trợ mobile cho chụp bài về sau.
- `PROPOSED` Khôi phục bản nháp phía server và cảnh báo thay đổi chưa lưu ở màn hình soạn đề.

## 4. Ràng buộc dữ liệu chung

- `PROPOSED` ID nội bộ dùng UUID; mã hiển thị là thuộc tính riêng, không dùng làm PK.
- `PROPOSED` Thời gian lưu dưới UTC (`timestamptz`), hiển thị theo múi giờ cấu hình.
- `PROPOSED` Điểm dùng `numeric(7,3)` và quy tắc làm tròn được ghi vào snapshot; mọi tổng điểm dùng decimal chính xác.
- `PROPOSED` Trường enum nghiệp vụ dùng `varchar` + CHECK hoặc bảng tham chiếu có kiểm soát để Flyway/JPA dễ tiến hóa; không dùng PostgreSQL native enum ở MVP.
