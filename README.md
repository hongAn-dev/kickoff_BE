### Backend Folder Structure

```
backend/
├── src/
│
│   ├── main/
│   │
│   │   ├── java/com/example/backend/
│   │   │
│   │   ├── BackendApplication.java
│   │   │   # Main class để chạy ứng dụng Spring Boot
│   │   │
│   │   ├── controller/
│   │   │   # Nhận request từ frontend (Angular)
│   │   │   # Ví dụ: /api/users
│   │   │
│   │   ├── service/
│   │   │   # Chứa business logic chính của hệ thống
│   │   │
│   │   ├── repository/
│   │   │   # Tầng giao tiếp với database
│   │   │   # Ví dụ: CRUD user trong PostgreSQL
│   │   │
│   │   ├── entity/
│   │   │   # Định nghĩa bảng database bằng Java class
│   │   │   # Ví dụ: User.java tương ứng bảng users
│   │   │
│   │   ├── dto/
│   │   │   # Các object dùng để trao đổi dữ liệu qua API
│   │   │   # Giúp không trả trực tiếp entity ra ngoài
│   │   │
│   │   ├── mapper/
│   │   │   # Chuyển đổi dữ liệu giữa entity và DTO
│   │   │
│   │   ├── exception/
│   │   │   # Xử lý lỗi chung của hệ thống
│   │   │   # Ví dụ: trả lỗi 404 khi user không tồn tại
│   │   │
│   │   ├── security/
│   │   │   # Cấu hình bảo mật
│   │   │   # Ví dụ: JWT authentication
│   │   │
│   │   ├── util/
│   │   │   # Các hàm tiện ích dùng chung
│   │   │
│   │
│   │   ├── resources/
│   │   │
│   │   ├── application.properties
│   │   │   # File cấu hình chính (database, port, jwt...)
│   │   │
│   │   ├── application-dev.yml
│   │   │   # Cấu hình môi trường development
│   │   │
│   │   ├── application-prod.yml
│   │   │   # Cấu hình môi trường production
│   │   │
│   │   └── db/migration/
│   │       # Các file SQL migration
│   │       # Dùng với Flyway hoặc Liquibase
│
│   ├── test/
│   │   └── java/com/example/backend/
│   │       # Chứa unit test / integration test
│
├── pom.xml
│   # File quản lý dependency của project
│
├── Dockerfile
│   # Build backend thành Docker container để deploy
│
└── README.md
```
