# Decision log GeoTeacher

## 1. Quyết định đã xác nhận

| ID | Trạng thái | Quyết định | Lý do / hệ quả |
|---|---|---|---|
| D-001 | CONFIRMED | GeoTeacher hỗ trợ một giáo viên Địa lý dạy trực tiếp, không phải LMS/thi online. | Giữ phạm vi MVP nhỏ và đúng nghiệp vụ. |
| D-002 | CONFIRMED | Java 21, Spring Boot/Maven, PostgreSQL/Flyway; React/TypeScript/Tailwind/shadcn. | Stack bàn giao. |
| D-003 | CONFIRMED | Modular monolith, package-by-feature; không microservices giai đoạn đầu. | Transaction đơn giản, vận hành phù hợp quy mô. |
| D-004 | CONFIRMED | MVP gồm M01–M05; M06–M08 và OMR/OCR/AI để sau. | Tập trung luồng lõi đề–chấm. |
| D-005 | CONFIRMED | Bốn loại câu hỏi; essay chấm thủ công trong MVP. | Nghiệp vụ lõi. |
| D-006 | CONFIRMED | Finalize đề tạo snapshot bất biến; sửa đề đã chốt tạo phiên bản mới. | Bảo toàn đề và điểm lịch sử. |
| D-007 | CONFIRMED | Mỗi mã đề lưu mapping đáp án riêng; giữ nhóm ngữ liệu/ràng buộc đảo. | Chấm đúng bản in thực tế. |
| D-008 | CONFIRMED | Chấm phải dùng snapshot đúng mã đề; giáo viên duyệt cuối. | Tránh kết quả thay đổi theo question bank/AI. |
| D-009 | CONFIRMED | Điểm đã chốt điều chỉnh có audit; không xóa cứng kết quả. | Truy vết và integrity. |
| D-021 | CONFIRMED (2026-10-13) | `class_code` chỉ duy nhất trong một năm học; cùng mã được phép dùng lại ở năm học khác. | Phản ánh cách đặt tên lớp lặp lại qua từng khóa và vẫn chặn nhầm lẫn trong cùng năm. |
| D-022 | CONFIRMED (2026-10-13) | AI hỗ trợ tạo đề theo thời lượng 15, 45 hoặc 60 phút và có thể dùng tài liệu/giáo trình giáo viên tải lên làm nguồn kiến thức. | AI chỉ tạo draft có nguồn để giáo viên duyệt; không tự finalize đề, công bố đáp án hay chốt điểm. |

## 2. Đề xuất kiến trúc cần duyệt

| ID | Trạng thái | Đề xuất | Lý do / phương án không chọn |
|---|---|---|---|
| D-010 | PROPOSED | Tách `exam_drafts`, immutable `exam_versions`, `exam_variants`. | Làm trạng thái và API rõ; không dùng một bảng mutable có nhiều cờ dễ sửa nhầm snapshot. |
| D-011 | PROPOSED | Snapshot hybrid: cột quan hệ cho identity/order/score + JSONB schema-versioned cho content/grading payload + checksum/file retention. | JSON-only khó FK/query; copy toàn bộ mô hình live quá cồng kềnh. |
| D-012 | PROPOSED | Grade dùng immutable `grade_versions` + item + audit, `grades` giữ con trỏ current. | Audit log before/after đơn thuần không bảo đảm tái dựng đầy đủ. |
| D-013 | PROPOSED | Tạo `exam_session_students` để snapshot roster. | Sửa tên/mã/chuyển lớp về sau không làm sai báo cáo lịch sử. |
| D-014 | PROPOSED | UUID cho PK; mã hiển thị tách riêng. | Không rò sequence, thuận lợi import/đồng bộ; bigint vẫn có thể dùng nếu ưu tiên đơn giản. |
| D-015 | PROPOSED | Session cookie same-origin thay JWT cho MVP. | Một web app/một người dùng, revoke/logout/CSRF quản lý rõ; JWT không mang lợi ích đủ lớn. |
| D-016 | PROPOSED | Grading strategy trả score + outcome/review metadata, không chỉ BigDecimal. | Short answer/recognition có trường hợp mơ hồ cần review. |
| D-017 | PROPOSED | `FileStorage` abstraction; local dev, object storage sau; snapshot giữ checksum và retention. | Di chuyển storage không chạm domain, tránh broken historical export. |
| D-018 | PROPOSED | PostgreSQL Testcontainers cho integration test. | H2 không phản ánh JSONB/partial index/constraint thực. |
| D-019 | PROPOSED | API version `/api/v1`, tách draft/version/variant và dùng adjustment command. | Ngăn update nhầm dữ liệu bất biến, thể hiện state transition. |
| D-020 | PROPOSED | Foundation hiện dùng Spring Boot 4.1.1 và React 19/Vite 8. | Các bản stable hiện tại khi khởi tạo; giữ Java source/target 21. Có thể điều chỉnh sớm nếu môi trường triển khai yêu cầu line khác. |
| D-023 | PROPOSED | Tài liệu được parse/chia đoạn, lập chỉ mục và truy xuất theo từng yêu cầu tạo đề (RAG); mỗi câu AI sinh lưu liên kết đoạn nguồn. | Giảm bịa kiến thức, hỗ trợ giáo viên kiểm chứng và không đưa toàn bộ tài liệu vào mọi prompt. |
| D-024 | PROPOSED | AI tạo `exam_draft`/question draft qua application service; đầu ra phải qua schema validation và các business validator hiện có. | Không cho model ghi trực tiếp database hoặc bỏ qua quy tắc tổng điểm, loại câu hỏi và snapshot. |

## 3. Câu hỏi mở ưu tiên

### P0 — cần chốt trước hoặc trong foundation

| ID | Trạng thái | Câu hỏi | Khuyến nghị hiện tại |
|---|---|---|---|
| O-001 | OPEN | Khôi phục mật khẩu qua email hay token một lần do chủ hệ thống tạo? | Nếu chưa có email service, dùng bootstrap/recovery token cục bộ có hạn, không endpoint công khai gửi email giả. |
| O-002 | OPEN | `student_code` unique toàn hệ thống hay theo năm học/trường? | Unique toàn hệ thống; học sinh qua năm bằng enrollment. |
| O-003 | OPEN | Học sinh có thể ở nhiều lớp cùng một năm không? | Cho phép về mặt dữ liệu, nhưng cảnh báo/chặn theo loại lớp sau khi có nghiệp vụ. |
| O-004 | OPEN | Import lỗi: all-or-nothing hay nhận các dòng hợp lệ? | Preview đầy đủ, mặc định all-or-nothing; thêm partial mode sau nếu cần. |
| O-005 | OPEN | Decimal API là JSON number hay string? | String cho contract điểm/tolerance để tránh client binary rounding, dùng schema rõ. |

### P1 — phải chốt trước M03/M04

| ID | Trạng thái | Câu hỏi | Khuyến nghị hiện tại |
|---|---|---|---|
| O-006 | OPEN | TRUE_FALSE dùng score ratio hay điểm tuyệt đối; blank/multiple xử lý thế nào? | Ratio × max score; blank/invalid = 0 và review trước finalize. |
| O-007 | OPEN | Short answer: rounding mode/scale, percent, absolute/relative tolerance và unit mặc định? | Cấu hình từng câu; HALF_UP; không đổi `% ↔ decimal` hay unit nếu chưa khai báo. |
| O-008 | OPEN | Có cho một question lặp lại trong cùng đề không? | Không mặc định; trường hợp chung ngữ liệu dùng group, không duplicate question. |
| O-009 | OPEN | Phạm vi unique mã đề và nguồn sinh mã? | Unique trong exam version; hệ thống gợi ý, giáo viên được sửa trước publish. |
| O-010 | OPEN | Có sinh thêm variant sau khi version đã dùng chấm? | Không; tạo revision/new session policy để giảm rủi ro. |
| O-011 | OPEN | PDFBox hay OpenPDF? | Làm spike với font tiếng Việt, bảng/ảnh trước khi chọn. |
| O-016 | OPEN | Dùng nhà cung cấp/model AI nào, chạy cloud hay local? | Bắt đầu sau một adapter trung lập; đánh giá chất lượng tiếng Việt, chi phí, tốc độ và điều khoản dữ liệu trước khi chọn. |
| O-017 | OPEN | Tài liệu tải lên được gửi/lưu tại dịch vụ AI trong bao lâu? | Mặc định không cho phép dùng để huấn luyện; cấu hình retention tối thiểu và cảnh báo rõ trước khi gửi ra ngoài. |
| O-018 | OPEN | Cấu hình mặc định cho đề 15/45/60 phút gồm số câu, loại câu, ma trận và thang điểm nào? | Dùng preset có thể chỉnh; giáo viên luôn xem ma trận và duyệt draft trước finalize. |
| O-019 | OPEN | Định dạng tài liệu AI hỗ trợ ở lát cắt đầu tiên? | Ưu tiên PDF/DOCX có text; OCR cho tài liệu scan để phase sau và báo rõ trang không đọc được. |

### P2 — phải chốt trước M05

| ID | Trạng thái | Câu hỏi | Khuyến nghị hiện tại |
|---|---|---|---|
| O-012 | OPEN | Bài vắng/miễn/làm lại/thay thế xử lý thế nào? | Attendance riêng; replace giữ liên kết bài cũ–mới, chỉ một official. |
| O-013 | OPEN | Nhập điểm phần/tổng có override không và ưu tiên nguồn điểm? | Cho override có lý do, không phân bổ ngược, không ghi đè item scores. |
| O-014 | OPEN | Regrade sau khi rubric/bug thay đổi tác động bài finalized ra sao? | Không tự động; preview và tạo grade version adjustment có reason/rule version. |
| O-015 | OPEN | Có cho điểm âm/bonus/vượt thang không? | Không trong MVP; mọi item 0..max và total 0..exam total. |

## 4. Deferred decisions

- `DEFERRED` Template OMR, confidence threshold và workflow ảnh.
- `DEFERRED` OCR provider/model; AI provider và data privacy/retention đã chuyển thành O-016/O-017 để chuẩn bị module AI đã xác nhận.
- `DEFERRED` Multi-user/RBAC/multi-tenant.
- `DEFERRED` Object-storage vendor, CDN và background queue ngoài process.
- `DEFERRED` Chính sách analytics nâng cao.

## 5. Cách cập nhật log

Khi người dùng chốt một mục, đổi trạng thái tương ứng sang `CONFIRMED`, ghi ngày và tóm tắt lý do. Nếu thay quyết định, không xóa dòng cũ: đánh dấu `SUPERSEDED` trong nội dung và trỏ đến ID mới.
