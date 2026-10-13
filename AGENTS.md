# Hướng dẫn làm việc với repository GeoTeacher

## Mục tiêu

GeoTeacher là hệ thống hỗ trợ **một giáo viên Địa lý** quản lý lớp, ngân hàng câu hỏi, đề kiểm tra trên giấy và điểm số. Đây không phải LMS, nền tảng bán khóa học hay hệ thống thi trực tuyến.

## Nguồn sự thật

Trước khi phân tích hoặc sửa mã, đọc theo thứ tự:

1. `docs/PROJECT_CONTEXT.md` — phạm vi và trạng thái hiện tại.
2. `docs/REQUIREMENTS.md` — yêu cầu chức năng/phi chức năng.
3. `docs/BUSINESS_RULES.md` — quy tắc nghiệp vụ bắt buộc.
4. `docs/DECISIONS.md` — quyết định đã chốt, đề xuất và câu hỏi mở.
5. Tài liệu chuyên biệt còn lại trong `docs/`.

Nếu tài liệu mâu thuẫn, không tự chọn một cách âm thầm. Ưu tiên yêu cầu mới nhất đã được người dùng xác nhận và cập nhật lại tài liệu liên quan.

## Nhãn trạng thái

- `CONFIRMED`: đã được người dùng xác nhận; không tự ý thay đổi.
- `PROPOSED`: phương án khuyến nghị, cần người dùng duyệt trước khi coi là ràng buộc.
- `OPEN`: còn thiếu quyết định nghiệp vụ/kỹ thuật.
- `DEFERRED`: ngoài MVP hoặc để giai đoạn sau.

Không nâng `PROPOSED`/`OPEN` thành `CONFIRMED` nếu chưa có xác nhận rõ ràng.

## Quy tắc thực hiện

- Luôn kiểm tra source và `git status` trước khi sửa; bảo toàn thay đổi không liên quan của người dùng.
- Phát triển theo từng lát cắt nhỏ trong kế hoạch, không sinh toàn bộ hệ thống trong một lần.
- Không xóa dữ liệu, không triển khai production và không thay đổi phạm vi nghiệp vụ khi chưa được phép.
- Backend là nơi thực thi business validation; frontend chỉ hỗ trợ trải nghiệm và phản hồi sớm.
- Controller chỉ xử lý giao thức; application/service điều phối use case và transaction; domain chứa quy tắc; repository xử lý lưu trữ.
- Không trả JPA entity trực tiếp qua API. Không đưa đáp án/rubric bí mật vào API danh sách hoặc API không cần quyền xem đáp án.
- Dùng `BigDecimal`/`numeric`, không dùng số thực nhị phân cho điểm.
- Mọi thay đổi schema phải qua Flyway; giữ migration bất biến sau khi đã dùng ở môi trường chung.
- Đề đã chốt, snapshot và mapping mã đề là bất biến. Không để sửa câu hỏi ngân hàng làm đổi đề/điểm lịch sử.
- Điểm đã chốt không được sửa mất dấu vết; điều chỉnh phải tạo phiên bản điểm mới và audit trong cùng transaction.
- Có validation và test cho mọi quy tắc tạo đề/chấm điểm quan trọng, đặc biệt biên số học và state transition.
- Giải thích ngắn bằng tiếng Việt cho class/luồng quan trọng: mục đích, lý do tồn tại, luồng Controller–Service–Repository và trade-off.

## Kiến trúc mục tiêu

- Modular monolith, Java 21 + Spring Boot + Maven + PostgreSQL + Flyway.
- React + TypeScript + Tailwind + shadcn/ui + React Router + TanStack Query + React Hook Form/Zod.
- Backend package-by-feature dưới `com.geoteacher`: `auth`, `configuration`, `classroom`, `question`, `exam`, `grading`, `file`, và `common` tối thiểu.
- Các module giai đoạn sau: `document`, `geography`, `ai`, `recognition`.
- Không tạo dependency vòng giữa module; giao tiếp qua application API/event nội bộ rõ ràng.
- File nhị phân nằm sau abstraction lưu trữ; database lưu metadata, checksum và khóa lưu trữ.

## Definition of Done cho một lát cắt

- Acceptance criteria và business rule liên quan đã rõ.
- Migration, entity/repository, service, API DTO và UI (nếu thuộc phạm vi) nhất quán.
- Unit test cho domain/service; integration test cho persistence/API có rủi ro cao.
- Xử lý lỗi có mã ổn định; dữ liệu bí mật không bị lộ.
- Tài liệu trạng thái/kế hoạch/quyết định được cập nhật nếu có thay đổi quan trọng.

## Tài liệu dự án

- `docs/PROJECT_CONTEXT.md`
- `docs/REQUIREMENTS.md`
- `docs/USE_CASES.md`
- `docs/BUSINESS_RULES.md`
- `docs/DATABASE_DESIGN.md`
- `docs/ARCHITECTURE.md`
- `docs/DECISIONS.md`
- `docs/IMPLEMENTATION_PLAN.md`
