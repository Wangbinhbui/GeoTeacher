# Kế hoạch triển khai GeoTeacher MVP

> Hiện trạng 2026-10-14: **M02 đã chạy local, M03 đã bắt đầu và nền document ingestion đã có.** Authentication, quản lý lớp/học sinh, import/export roster, question bank `SINGLE_CHOICE` và upload/trích xuất PDF-DOCX đã có; ba loại câu hỏi còn lại, M04–M05 và model AI chưa triển khai.

## 0. Nguyên tắc kế hoạch

- Đi theo vertical slice nhỏ có thể chạy/test, nhưng dependency nền phải xong trước.
- Mỗi milestone có migration, backend, frontend và test cần thiết; không để validation chỉ ở UI.
- Không chen OMR/OCR/AI vào luồng lõi M01–M05; AI tạo đề bám tài liệu là phase mở rộng đã xác nhận và chỉ bắt đầu sau nền question/exam/document.
- Trước mỗi phase, chốt các `OPEN` liên quan trong `DECISIONS.md`.

## Phase 0 — Chốt thiết kế và bootstrap

### 0.1. Duyệt quyết định

- [ ] Chốt O-001 đến O-005.
- [ ] Duyệt D-010 đến D-019 hoặc ghi phương án thay thế.
- [ ] Chốt convention API/error/decimal/timezone.

### 0.2. Khởi tạo kỹ thuật

- [x] Khởi tạo Git, `.gitignore`, README và sample env không chứa secret.
- [x] Tạo `backend/`, `frontend/`, `compose.yaml` cho PostgreSQL local.
- [x] Spring Boot Java 21/Maven: Web, Validation, Security, Data JPA, Flyway, PostgreSQL, test.
- [x] React TypeScript: Router, Query, RHF/Zod, Tailwind và UI primitives theo hướng shadcn/ui.
- [x] CI nền: backend unit test, frontend lint/typecheck/build.
- [ ] Error envelope, logging/correlation, health check, test infrastructure.

**Exit:** app rỗng chạy local, Flyway kết nối PostgreSQL, test/CI xanh; chưa có feature giả.

## Phase 1 — M01 + M02 Foundation

### Slice 1: Authentication

- [x] User bootstrap an toàn, login/logout/session, password hashing.
- [x] Đổi mật khẩu; recovery chờ O-001.
- [ ] Security integration tests: CSRF, unauthenticated access, answer/private endpoint baseline.

### Slice 2: School year/class

- [x] Migration + CRUD năm học/lớp + archive.
- [x] UI danh sách/form, validation trùng và optimistic conflict.
- [x] Chốt phạm vi mã lớp: unique trong một năm học, được lặp ở năm khác.

### Slice 3: Student/enrollment

- [x] CRUD student, enrollment, rời lớp và tìm kiếm.
- [x] Chốt O-002/O-003; unique constraint và domain/service test.
- [x] Import preview/confirm Excel, file mẫu và export roster; O-004 dùng all-or-nothing.

**Exit:** giáo viên quản lý được roster nhiều năm học mà dữ liệu cũ không mất.

## Phase 2 — M03 Question Bank

### 2.1. Taxonomy và file

- [ ] Topic tree và cognitive level.
- [x] `FileStorage` local + metadata/checksum cho tài liệu nguồn.
- [x] Upload PDF/DOCX validation và cleanup bù trừ khi transaction lỗi; job quét orphan định kỳ chưa có.

### 2.2. Bốn loại câu hỏi

- [x] Base question + single choice: CRUD/search/archive, optimistic conflict và danh sách không lộ đáp án.
- [ ] True/false + bảng quy đổi; chốt O-006.
- [ ] Short answer normalization/tolerance/unit; chốt O-007 và viết test bảng biên.
- [ ] Essay rubric/manual grading model.
- [ ] Status/archive, copy, filter/search/pagination và preview.

### 2.3. Import

- [ ] Excel template + preview/report.
- [ ] Word import chỉ cho format xác định; trường hợp mơ hồ trả lỗi/cảnh báo.

**Exit:** bốn loại câu hỏi hợp lệ, tìm/tái sử dụng được; test không cho cấu hình chấm thiếu/sai.

## Phase 3 — M04 Exam Creation

### 3.1. Draft editor

- [ ] Exam catalog/draft/section/question/group, tổng điểm và concurrency.
- [ ] UI desktop-first: metadata, sections, question picker, selected list, score summary.

### 3.2. Matrix

- [ ] Matrix rules, availability query, deterministic selection và báo từng ô thiếu.
- [ ] Test không nới filter âm thầm.

### 3.3. Finalize/snapshot

- [ ] Snapshot schema version/canonical checksum/file retention.
- [ ] Transaction finalize idempotent và immutable persistence tests.
- [ ] Revision từ finalized version.

### 3.4. Variants

- [ ] Chốt O-008–O-010.
- [ ] Pure permutation generator + group/option policies.
- [ ] Mapping/answer key persistence và property/invariant tests.

### 3.5. Export

- [ ] DOCX spike và template.
- [ ] PDF spike chốt O-011; preview/export đề và answer key tách quyền/endpoint.
- [ ] Golden-file/render checks cho tiếng Việt, bảng, ảnh.

**Exit:** tạo, chốt, revision, nhiều mã và xuất đề; sửa question live không đổi bất kỳ artifact finalized nào.

## Phase 4 — M05 Grading & Score

### 4.1. Session/roster/submission

- [ ] Exam session từ class + finalized version; roster snapshot.
- [ ] Submission/variant validation, chống trùng, state machine.

### 4.2. Grading engine

- [ ] Strategy single choice/true-false/short-answer/essay manual.
- [ ] Answer normalization, review outcome, item/source score.
- [ ] Deterministic re-run tests bằng snapshot.

### 4.3. Manual grading/finalize

- [ ] UI danh sách học sinh/trạng thái, nhập đáp án/điểm/comment.
- [ ] Chốt O-012–O-015.
- [ ] Completeness/limit validation, grade version finalize.

### 4.4. Adjustment/audit/import/export

- [ ] Adjustment command tạo version + audit nguyên tử.
- [ ] Quy trình replace submission nếu được duyệt.
- [ ] Import preview điểm và export bảng điểm Excel.

**Exit:** một đợt kiểm tra giấy đi trọn luồng đến điểm finalized/export; điều chỉnh vẫn tái dựng được lịch sử.

## Phase 5 — Hardening và nghiệm thu MVP

- [ ] E2E luồng chính và ngoại lệ quan trọng.
- [ ] Kiểm tra answer/data leakage, upload, dependency vulnerabilities.
- [ ] Query profiling/N+1/index với dữ liệu đại diện.
- [ ] Backup + restore rehearsal; migration test từ database rỗng.
- [ ] Accessibility/responsive smoke test; browser support.
- [ ] Hướng dẫn vận hành local/deploy staging; không tự deploy production.

## Backlog sau MVP

- `DEFERRED` M06 Teaching Documents.
- `DEFERRED` M07 Geography Tools.
- `CONFIRMED` Phase AI: upload/index PDF-DOCX có text; tạo đề draft 15/45/60 phút bám tài liệu, có trích nguồn và giáo viên duyệt.
- `DEFERRED` OMR/OCR, mobile capture và AI chấm essay.
- `DEFERRED` Analytics nâng cao và object storage production.

## Phase AI — Document-grounded exam drafting

1. Chốt O-016–O-019 và đánh giá quyền riêng tư/chi phí/chất lượng tiếng Việt. **Đang mở; không chặn ingestion local không gọi AI.**
2. [x] Xây `document` + `file` ingestion: upload an toàn PDF/DOCX có text, parse, chia đoạn, checksum, trạng thái và báo tài liệu không có text/OCR.
3. [x] Retrieval scope ban đầu theo tối đa 8 giáo trình, tối đa 24 đoạn và citation bằng chunk ID; semantic/vector ranking để sau.
4. [x] Chọn mục đích đề và thời lượng 15/45/60 phút; ma trận chi tiết theo từng preset vẫn `OPEN` và có ô yêu cầu tùy chỉnh.
5. [x] Adapter model trả structured draft; backend validate loại câu, đáp án, citation và tổng điểm 10 trước khi lưu.
6. [ ] UI review đã hiển thị nguồn và đáp án; chỉnh/chấp nhận từng câu và chuyển sang M04 chưa triển khai.
7. Eval bằng bộ giáo trình đại diện: groundedness, độ phủ ma trận, độ khó, tiếng Việt, duplicate và rò rỉ đáp án.

## Nên bắt đầu code từ đâu?

Sau khi duyệt tài liệu, bắt đầu bằng **Phase 0.2**, rồi vertical slice **Authentication → School year/Class → Student/Enrollment**. Không nên bắt đầu từ grading engine trước vì nó phụ thuộc snapshot/cấu hình câu hỏi/variant chưa tồn tại. Tuy nhiên ngay từ bootstrap phải dựng test PostgreSQL/Flyway và các primitive decimal/time/error để tránh sửa nền về sau.

## Definition of Done mỗi task

- Migration + rollback strategy (Flyway thường roll-forward) đã review.
- Backend validation/domain test/integration test tương xứng rủi ro.
- API/OpenAPI và frontend type/form thống nhất.
- Không lộ secret/answer/student data; error có mã ổn định.
- Docs/decision/status được cập nhật nếu task tạo quyết định mới.
