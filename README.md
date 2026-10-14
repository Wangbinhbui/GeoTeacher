# GeoTeacher

GeoTeacher là hệ thống hỗ trợ một giáo viên Địa lý quản lý lớp học, ngân hàng câu hỏi, đề kiểm tra trên giấy và kết quả chấm.

## Trạng thái

Source hiện tại cung cấp:

- Backend Java 21, Spring Boot, Spring Security, Spring Data JPA, Flyway và PostgreSQL.
- Xác thực bằng server-side session, CSRF cookie, bootstrap tài khoản đầu tiên và đổi mật khẩu.
- Frontend React/TypeScript, Vite, Tailwind CSS, React Router, TanStack Query, React Hook Form và Zod.
- Màn hình đăng nhập, dashboard, quản lý năm học/lớp và học sinh/xếp lớp bằng các UI primitive theo hướng shadcn/ui.
- CRUD/archive năm học và lớp, optimistic conflict, mã lớp duy nhất trong từng năm học và được lặp ở năm khác.
- CRUD/archive/tìm kiếm học sinh; mã học sinh duy nhất toàn hệ thống; ghi danh nhiều lớp, rời lớp có lưu lịch sử.
- Import học sinh Excel có file mẫu, preview từng dòng, confirm all-or-nothing và export roster theo lớp.
- Ngân hàng câu hỏi bước đầu hỗ trợ CRUD/search/archive câu trắc nghiệm một đáp án, metadata khối/chủ đề/mức độ và không lộ đáp án ở API danh sách.
- Khu vực **Giáo án & tài liệu** hỗ trợ tải PDF/DOCX có text tối đa 20 MB, checksum chống trùng, trích xuất/chia đoạn và hiển thị nhãn nguồn để giáo viên kiểm tra.
- Khu vực **Đề kiểm tra** tạo draft AI theo 5 mục đích và thời lượng 15/45/60 phút, bám giáo trình được chọn, tùy chọn học form từ đề mẫu và hiển thị citation để giáo viên duyệt.
- Docker Compose cho PostgreSQL, backend và frontend.

Các loại câu hỏi còn lại, luồng đề finalized và chấm điểm sẽ được phát triển theo [kế hoạch MVP](docs/IMPLEMENTATION_PLAN.md). Preset ma trận chi tiết, chính sách retention chính thức và OCR cho PDF scan vẫn chưa chốt/triển khai.
AI mặc định tắt. Để dùng adapter Gemini, đặt `AI_ENABLED=true`, `GEMINI_API_KEY` và tùy chọn `GEMINI_MODEL` trong `.env`, rồi chạy lại `docker compose up -d --build backend`. Mỗi lần tạo đề vẫn phải xác nhận gửi các đoạn đã chọn sang Gemini; draft không tự finalize. Không commit `.env` hoặc khóa API lên GitHub.

## Chạy bằng Docker Compose

1. Sao chép `.env.example` thành `.env` và thay toàn bộ mật khẩu mẫu.
2. Chạy:

```bash
docker compose up --build
```

3. Mở `http://localhost:3000` và đăng nhập bằng tài khoản bootstrap trong `.env`.

## Chạy từng phần khi phát triển

Yêu cầu Java 21 và Node.js tương thích Vite.

```bash
docker compose up db
cd backend
./mvnw spring-boot:run
```

Ở terminal khác:

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server proxy `/api` và `/actuator` sang backend ở cổng `8080`.
Khi chạy backend ngoài Compose, file tải lên mặc định nằm tại `./data/files`; có thể đổi bằng `FILE_STORAGE_ROOT`. Compose dùng volume `geoteacher_files` để giữ file qua lần khởi động lại.

## Kiểm thử

```bash
cd backend && ./mvnw test
cd frontend && npm run lint && npm run build
```

Không commit `.env`. Nếu không cung cấp `GEOTEACHER_BOOTSTRAP_ADMIN_PASSWORD`, backend sẽ không tự tạo tài khoản quản trị đầu tiên.
