# Quy tắc nghiệp vụ GeoTeacher

## 1. Đề và snapshot

### Quy tắc đã xác nhận

- `CONFIRMED` **BR-EX-01**: Đề phải có đủ metadata bắt buộc.
- `CONFIRMED` **BR-EX-02**: Đề có ít nhất một câu hỏi.
- `CONFIRMED` **BR-EX-03**: Tổng điểm hợp lệ và bằng tổng điểm phần/câu; mặc định thang 10.
- `CONFIRMED` **BR-EX-04**: `SINGLE_CHOICE` có đúng một đáp án đúng.
- `CONFIRMED` **BR-EX-05**: Mọi mệnh đề `TRUE_FALSE` có đáp án.
- `CONFIRMED` **BR-EX-06**: `SHORT_ANSWER` có đáp án hoặc hướng dẫn chấm.
- `CONFIRMED` **BR-EX-07**: `ESSAY` có rubric hoặc hướng dẫn chấm.
- `CONFIRMED` **BR-EX-08**: Chỉ sinh mã đề khi đề hợp lệ.
- `CONFIRMED` **BR-EX-09**: Mỗi mã đề có mapping đáp án riêng.
- `CONFIRMED` **BR-EX-10**: Không sửa trực tiếp phiên bản đã chốt và sử dụng.
- `CONFIRMED` **BR-EX-11**: Sửa đề đã chốt tạo phiên bản mới.
- `CONFIRMED` **BR-EX-12**: Export giữ nội dung, bảng, hình và biểu đồ.

### Bổ sung đề xuất

- `PROPOSED` **BR-EX-13**: Finalize là transaction nguyên tử; thất bại ở snapshot/attachment nào thì không có version nửa chừng.
- `PROPOSED` **BR-EX-14**: Snapshot gồm schema version, metadata đề, section, question content, các item/option, answer, grading config, rubric, max score, attachment metadata/checksum và thứ tự.
- `PROPOSED` **BR-EX-15**: Bản ghi snapshot không hỗ trợ API update/delete. File được snapshot tham chiếu không được xóa vật lý trong thời gian lưu trữ.
- `PROPOSED` **BR-EX-16**: Snapshot có checksum canonical; checksum được kiểm tra trước khi chấm/export để phát hiện corruption, không dùng thay cho constraint bất biến.
- `PROPOSED` **BR-EX-17**: Finalized/archived chỉ khác khả năng sử dụng mới; archive không thay đổi dữ liệu snapshot.

## 2. Tạo nhiều mã đề

- `CONFIRMED` Không làm đổi ý nghĩa câu hỏi, đáp án hay mối quan hệ với ngữ liệu chung.
- `PROPOSED` Nhóm ngữ liệu (`shuffle_group`) là đơn vị đảo; câu trong nhóm giữ thứ tự tương đối trừ khi được cho phép rõ ràng.
- `PROPOSED` Mỗi câu/phương án có cờ `shuffleable`; mặc định false cho ngữ liệu/phần nhạy thứ tự và true cho option single-choice nếu hợp lệ.
- `PROPOSED` Mỗi variant lưu `seed`, thuật toán/version, vị trí in → snapshot question và vị trí nhãn → snapshot option. Không phụ thuộc vào việc chạy lại random để chấm.
- `PROPOSED` Answer key variant lưu bằng ID snapshot ổn định; nhãn A/B/C/D chỉ là trình bày được suy ra từ mapping.
- `PROPOSED` Sau khi variant đã có submission, mapping là bất biến. Sinh thêm variant là `OPEN` và mặc định không cho phép cho tới khi chốt chính sách.
- `PROPOSED` Generator phải qua invariant test: mỗi câu xuất hiện đúng một lần, mỗi option đúng một lần, đáp án sau permutation tương đương đáp án snapshot, nhóm không bị vỡ.

## 3. Chấm `TRUE_FALSE`

- `CONFIRMED` So sánh từng mệnh đề và dùng bảng quy đổi cấu hình được; ví dụ 0/1/2/3/4 đúng → 0/0.1/0.25/0.5/1 chỉ là minh họa, không phải mặc định cứng.
- `PROPOSED` Bảng quy đổi thuộc grading config của câu và được copy vào snapshot; có đủ khóa từ `0..N`, không trùng và điểm không giảm khi số ý đúng tăng.
- `PROPOSED` Mỗi giá trị bảng là tỷ lệ `0..1`; điểm câu = tỷ lệ × max score rồi áp dụng rounding của snapshot. Phương án lưu điểm tuyệt đối thay vì tỷ lệ còn `OPEN`.
- `OPEN` Mệnh đề bỏ trống hoặc đánh dấu không hợp lệ được tính sai hay chuyển review. Khuyến nghị MVP: tính 0 cho mệnh đề đó nhưng giữ cờ review trước khi finalize.
- `OPEN` Có cho partial credit theo từng mệnh đề độc lập ngoài bảng correct-count hay không. Khuyến nghị MVP: chỉ một mode/bảng trên mỗi câu.

## 4. Chấm `SHORT_ANSWER`

- `CONFIRMED` Chỉ coi tương đương khi có quy tắc rõ; hỗ trợ nhiều đáp án, sai số, đơn vị, làm tròn và chuẩn hóa số.
- `PROPOSED` Mỗi câu chọn rõ một mode: `TEXT_EXACT_SET`, `NUMERIC_TOLERANCE`, hoặc `MANUAL_REVIEW`; không đoán mode từ chuỗi nhập.
- `PROPOSED` Text normalization có pipeline được cấu hình và snapshot: Unicode NFC, trim, gộp whitespace; bỏ phân biệt hoa thường/dấu câu/dấu tiếng Việt chỉ khi bật. Không fuzzy/semantic matching ở MVP.
- `PROPOSED` Numeric parser chấp nhận dấu phẩy hoặc dấu chấm thập phân theo quy tắc locale nhưng từ chối chuỗi mơ hồ; lưu giá trị canonical `BigDecimal`.
- `PROPOSED` Unit chỉ được đổi qua alias/hệ số đã cấu hình cho câu; sai/thiếu đơn vị theo policy `REQUIRED`, `OPTIONAL`, `FORBIDDEN`. Không tự suy diễn đơn vị.
- `PROPOSED` So khớp theo một policy rõ: absolute tolerance hoặc relative tolerance; nếu dùng rounding, chuẩn hóa đơn vị → làm tròn theo snapshot → so sánh. Kết quả biên được test bằng `BigDecimal`.
- `OPEN` Chọn default scale/rounding mode, relative tolerance và cách xử lý đáp án như `10%` so với `0.1`. Khuyến nghị MVP: cấu hình từng câu, mặc định `HALF_UP`, không quy đổi phần trăm ngầm.
- `PROPOSED` Parse/rule không đủ chắc chắn trả `NEEDS_REVIEW`, không tự cho đúng/sai.

## 5. Chấm bài và điểm

- `CONFIRMED` **BR-GR-01**: Submission gắn học sinh và đợt kiểm tra.
- `CONFIRMED` **BR-GR-02/03**: Phải xác định mã đề; không auto-grade khi thiếu mã.
- `CONFIRMED` **BR-GR-04**: Không finalize khi còn lỗi nhận dạng chưa xử lý.
- `CONFIRMED` **BR-GR-05**: Bỏ trống/chọn nhiều đáp án theo quy tắc được cấu hình.
- `CONFIRMED` **BR-GR-06**: Chấm bằng answer snapshot của đúng mã đề.
- `CONFIRMED` **BR-GR-07/08**: Điểm câu không vượt max; tổng điểm chính xác.
- `CONFIRMED` **BR-GR-09**: Chỉ finalize khi hoàn tất phần bắt buộc.
- `CONFIRMED` **BR-GR-10**: Sửa điểm đã chốt có audit.
- `CONFIRMED` **BR-GR-11**: Không có hai submission chính thức trùng học sinh–đợt nếu không qua thay thế.
- `CONFIRMED` **BR-GR-12**: Không xóa cứng kết quả đã chốt.

### Nguồn điểm và ưu tiên

- `PROPOSED` Mỗi score item ghi `source`: `AUTO`, `MANUAL`, `IMPORT`, `ADJUSTMENT`; cùng `rule_version`/người nhập/thời điểm.
- `PROPOSED` Điểm thủ công không âm thầm bị auto-grade ghi đè. Regrade phải tạo preview diff và lệnh xác nhận.
- `OPEN` Nhập điểm từng phần/tổng có phân bổ ngược xuống câu hay chỉ là override. Khuyến nghị: không phân bổ ngầm; lưu override riêng kèm lý do.

### Lịch sử điểm

- `PROPOSED` Trước finalize có thể sửa draft answer/score với optimistic lock. Khi finalize, tạo `grade_version` bất biến gồm tổng, subtotal, item score và comment.
- `PROPOSED` Điều chỉnh sau finalize luôn tạo version kế tiếp với `supersedes_version_id`, lý do bắt buộc, actor/time và audit event trong cùng transaction.
- `PROPOSED` Không sửa/xóa grade version cũ. `grades.current_version_id` chỉ là con trỏ tiện truy vấn; lịch sử là nguồn kiểm chứng.

## 6. State machine đề xuất

### Exam version

`DRAFT → FINALIZED → ARCHIVED`

- `DRAFT`: được sửa.
- `FINALIZED`: snapshot bất biến, được sinh mã/xuất/dùng tạo đợt.
- `ARCHIVED`: bất biến, không dùng cho đợt mới nhưng lịch sử vẫn truy cập.
- Chỉnh một bản finalized tạo draft revision mới; không chuyển ngược.

`OPEN`: archive toàn bộ exam hay từng version; khuyến nghị archive từng version và có trạng thái catalog ở exam.

### Submission

`PENDING → PARTIALLY_GRADED | NEEDS_REVIEW → GRADED → FINALIZED`

- `PENDING`: đã tạo, chưa đủ dữ liệu/chưa chấm.
- `PARTIALLY_GRADED`: có điểm nhưng còn phần bắt buộc.
- `NEEDS_REVIEW`: có dữ liệu mơ hồ/lỗi cần giáo viên xử lý; có thể quay lại trạng thái phù hợp trước finalize.
- `GRADED`: đầy đủ, tổng hợp xong nhưng chưa chốt.
- `FINALIZED`: khóa submission và grade version hiện hành.

`PROPOSED`: `REPLACED` là trạng thái terminal khi có quy trình làm lại/thay bài. `OPEN`: chính sách cụ thể.

## 7. Transaction bắt buộc

- `PROPOSED` Finalize exam + tạo toàn bộ snapshot/checksum.
- `PROPOSED` Publish/generate một variant + mappings/answer key.
- `PROPOSED` Tạo submission cùng constraint chống trùng.
- `PROPOSED` Auto-grade ghi tất cả item scores và tổng dự thảo.
- `PROPOSED` Finalize grade và adjustment grade version/audit.
- `PROPOSED` Import chỉ commit sau bước preview/confirm theo chính sách import đã chọn.
