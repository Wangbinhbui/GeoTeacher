# GeoTeacher

GeoTeacher là hệ thống hỗ trợ một giáo viên Địa lý quản lý lớp học, ngân hàng câu hỏi, đề kiểm tra trên giấy và kết quả chấm.

## Trạng thái

Commit nền đầu tiên cung cấp:

- Backend Java 21, Spring Boot, Spring Security, Spring Data JPA, Flyway và PostgreSQL.
- Xác thực bằng server-side session, CSRF cookie, bootstrap tài khoản đầu tiên và đổi mật khẩu.
- Frontend React/TypeScript, Vite, Tailwind CSS, React Router, TanStack Query, React Hook Form và Zod.
- Màn hình đăng nhập và dashboard khung bằng các UI primitive theo hướng shadcn/ui.
- Docker Compose cho PostgreSQL, backend và frontend.

Các module lớp học, câu hỏi, đề và chấm điểm sẽ được phát triển theo [kế hoạch MVP](docs/IMPLEMENTATION_PLAN.md).

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

## Kiểm thử

```bash
cd backend && ./mvnw test
cd frontend && npm run lint && npm run build
```

Không commit `.env`. Nếu không cung cấp `GEOTEACHER_BOOTSTRAP_ADMIN_PASSWORD`, backend sẽ không tự tạo tài khoản quản trị đầu tiên.
