# Use case GeoTeacher

## 1. Quy ước

- Actor chính: **Giáo viên Địa lý**.
- Điều kiện chung: giáo viên đã đăng nhập, trừ use case xác thực.
- Nhãn `CONFIRMED` mô tả phạm vi đã bàn giao; chi tiết `PROPOSED` là cách hoàn thiện luồng để triển khai an toàn.

## 2. M01 — Authentication & Configuration

### UC-AU-01 — Đăng nhập (`CONFIRMED`)

**Luồng chính:** nhập thông tin đăng nhập → hệ thống kiểm tra tài khoản/mật khẩu → tạo phiên an toàn → mở dashboard.

**Ngoại lệ:** sai thông tin trả lỗi chung, không tiết lộ tài khoản có tồn tại; tài khoản bị khóa/tạm giới hạn sau nhiều lần sai là `PROPOSED`.

### UC-AU-02 — Đổi/khôi phục mật khẩu (`CONFIRMED`, cơ chế recovery `OPEN`)

**Luồng đổi:** xác nhận mật khẩu hiện tại → kiểm tra mật khẩu mới → hash và lưu → vô hiệu các phiên khác (`PROPOSED`).

**Luồng khôi phục:** yêu cầu token một lần có hạn dùng → xác minh → đặt mật khẩu mới → token bị tiêu thụ.

### UC-CF-01 — Cập nhật cấu hình (`CONFIRMED`)

Giáo viên cập nhật hồ sơ, trường, năm học mặc định, thang điểm và mẫu đề. Thay đổi cấu hình mặc định không hồi tố vào snapshot/điểm cũ.

## 3. M02 — Classroom & Student

### UC-CL-01 — Quản lý lớp (`CONFIRMED`)

Tạo lớp trong năm học, sửa metadata khi chưa archive, lưu trữ lớp. Hệ thống chặn mã lớp trùng trong phạm vi đã chọn (`PROPOSED`: cùng năm học).

### UC-ST-01 — Quản lý học sinh/ghi danh (`CONFIRMED`)

Tạo hoặc tìm học sinh → ghi danh vào lớp → cập nhật thông tin/trạng thái. Hệ thống không xóa cứng học sinh đã có submission/grade.

### UC-ST-02 — Import học sinh (`CONFIRMED`)

1. Tải mẫu, điền và upload Excel.
2. Hệ thống parse vào vùng tạm, kiểm tra từng dòng và hiển thị preview.
3. Giáo viên xác nhận.
4. Hệ thống ghi toàn bộ hợp lệ trong một transaction hoặc áp dụng chính sách partial import đã chọn (`OPEN`).

Ngoại lệ: file sai mẫu, dòng trùng, mã không hợp lệ, lớp/năm học không tồn tại.

## 4. M03 — Question Bank

### UC-QS-01 — Tạo/sửa câu hỏi (`CONFIRMED`)

1. Chọn loại, khối, chủ đề và mức nhận thức.
2. Nhập nội dung cùng cấu hình riêng theo loại.
3. Thêm tệp/bảng/ngữ liệu nếu có.
4. Hệ thống validate cấu trúc/đáp án/rubric.
5. Lưu nháp hoặc kích hoạt (`PROPOSED`).

Ngoại lệ: single choice không đúng một đáp án; true/false thiếu đáp án mệnh đề hoặc bảng điểm; short answer không có đáp án/hướng dẫn; essay không rubric/hướng dẫn.

### UC-QS-02 — Tìm, lọc, sao chép và archive (`CONFIRMED`)

Kết quả có phân trang; bản sao có ID mới. Archive không ảnh hưởng snapshot đề cũ. Xóa cứng chỉ cân nhắc cho bản nháp chưa từng được tham chiếu (`PROPOSED`).

### UC-QS-03 — Import câu hỏi (`CONFIRMED`)

Upload file theo template → parse/validate → preview lỗi/cảnh báo → xác nhận import → báo cáo kết quả. Import Word chỉ nhận cấu trúc mà parser xác định chắc chắn; phần mơ hồ không được tự suy đoán.

## 5. M04 — Exam

### UC-EX-01 — Tạo đề nháp (`CONFIRMED`)

1. Nhập metadata và cấu trúc các phần.
2. Chọn câu từ ngân hàng hoặc tạo câu mới.
3. Sắp thứ tự/nhóm ngữ liệu, cấu hình điểm.
4. Hệ thống tính tổng và cảnh báo vi phạm.
5. Lưu nháp.

### UC-EX-02 — Tạo và áp dụng ma trận (`CONFIRMED`)

1. Khai báo từng ô theo chủ đề/bài, loại, mức độ, số câu và điểm.
2. Hệ thống kiểm tra tính nhất quán tổng số câu/tổng điểm.
3. Truy vấn ứng viên chính xác cho từng ô.
4. Chọn có seed để có thể tái hiện (`PROPOSED`).
5. Nếu thiếu, trả về từng ô thiếu và không tự nới bộ lọc.

### UC-EX-03 — Chốt đề (`CONFIRMED`)

1. Giáo viên yêu cầu finalize.
2. Hệ thống khóa logic bản nháp, validate đầy đủ và tính checksum.
3. Trong một transaction, tạo exam version + section/question/grading/attachment snapshots.
4. Đánh dấu phiên bản `FINALIZED`; dữ liệu snapshot từ đây bất biến.

Ngoại lệ: không có câu, tổng điểm sai, đáp án/rubric thiếu, attachment không khả dụng, yêu cầu finalize lặp. `PROPOSED`: dùng idempotency key/checksum để request lặp trả cùng kết quả.

### UC-EX-04 — Sinh mã đề (`CONFIRMED`)

1. Chọn số lượng/mã và chính sách đảo.
2. Hệ thống tạo permutation theo seed, coi nhóm ngữ liệu là một đơn vị.
3. Chỉ đảo những câu/phương án được phép.
4. Ghi mapping câu, phương án và answer key của từng mã.
5. Validate round-trip mapping trước khi phát hành.

Ngoại lệ: trùng mã, ràng buộc không cho phép đủ biến thể, mapping mất đáp án, yêu cầu sửa phiên bản đã dùng.

### UC-EX-05 — Tạo revision (`CONFIRMED`)

Từ đề đã chốt, hệ thống sao chép dữ liệu cần thiết thành một draft version mới; phiên bản cũ giữ nguyên. Sau khi sửa, giáo viên finalize như một phiên bản độc lập.

### UC-EX-06 — Xuất đề/đáp án (`CONFIRMED`)

Chọn version + mã đề + template → tạo Word/PDF. Đáp án là tài nguyên riêng, chỉ endpoint/command có chủ đích mới trả về. Lỗi render không thay đổi trạng thái đề.

## 6. M05 — Grading

### UC-GR-01 — Tạo đợt kiểm tra (`CONFIRMED`)

Chọn lớp, exam version đã chốt, thời điểm và thông tin kiểm tra. `PROPOSED`: chụp snapshot roster hoặc ít nhất lưu enrollment ID để thay đổi lớp sau này không làm sai lịch sử.

### UC-GR-02 — Nhập bài làm/đáp án (`CONFIRMED`)

1. Chọn học sinh và mã đề.
2. Tạo submission duy nhất cho cặp học sinh–đợt.
3. Nhập đáp án theo thứ tự in; hệ thống ánh xạ về snapshot question bằng variant mapping.
4. Lưu bản nháp; câu mơ hồ/không hợp lệ chuyển `NEEDS_REVIEW`.

Ngoại lệ: học sinh không thuộc roster, mã đề không thuộc version, bài trùng, bỏ trống hoặc chọn nhiều đáp án.

### UC-GR-03 — Tự chấm phần khách quan (`CONFIRMED`)

Hệ thống dùng snapshot và cấu hình đúng mã đề, chọn strategy theo loại, tạo kết quả từng câu rồi tổng hợp. Không ghi đè điểm thủ công đã được khóa nếu không qua lệnh regrade có audit (`PROPOSED`).

### UC-GR-04 — Chấm tự luận/nhập điểm (`CONFIRMED`)

Giáo viên nhập điểm từng tiêu chí/câu, nhận xét và nguồn điểm. Hệ thống kiểm tra giới hạn, độ đầy đủ và tính tổng decimal.

### UC-GR-05 — Chốt điểm (`CONFIRMED`)

1. Kiểm tra mã đề, lỗi review, phần bắt buộc và tổng điểm.
2. Tạo grade version bất biến từ kết quả hiện tại.
3. Đánh dấu submission/grade `FINALIZED` trong cùng transaction.

### UC-GR-06 — Điều chỉnh điểm đã chốt (`CONFIRMED`)

Giáo viên nhập lý do → hệ thống không sửa version cũ → tạo grade version mới, liên kết version trước, ghi actor/time/reason/before-after → cập nhật current version nguyên tử.

### UC-GR-07 — Import/export điểm (`CONFIRMED`)

Import theo preview và validation tương tự học sinh; export kết quả đúng version hiện hành và có dấu thời điểm xuất.

### UC-GR-08 — Thay thế bài làm (`PROPOSED`, chính sách `OPEN`)

Bài cũ chuyển `REPLACED`, liên kết bài mới; unique partial index đảm bảo chỉ một bài chính thức hiện hành. Không xóa bài/điểm cũ.

## 7. Use case giai đoạn sau

- `DEFERRED` Upload ảnh → nhận dạng OMR/OCR → review confidence → chấm.
- `DEFERRED` AI đề xuất câu hỏi/điểm tự luận/nhận xét; giáo viên duyệt cuối.
- `DEFERRED` Quản lý giáo án, tài liệu và công cụ bản đồ/biểu đồ Địa lý.
