# PROJECT CONTEXT — Kickoff + Module Thông Báo Tình Hình

## STACK
- **Frontend**: Angular 21 (standalone components, SSR), TailwindCSS v4, TypeScript — deploy Vercel
- **Backend**: Spring Boot / Java 21, JPA/Hibernate, PostgreSQL — deploy Railway
- **DB**: Neon (PostgreSQL cloud), Hikari pool (max 5 connections)
- **Proxy**: `vercel.json` rewrite `/api/*` → Railway (giải quyết CORS + SPA fallback)

---

## CẤU TRÚC BACKEND HIỆN TẠI

```
Controller → Service (interface) → ServiceImpl → Repository (JpaRepository) → Entity
```

- `common.ApiResponse<T>` — wrapper response: `{ success, message, data, timestamp }`
- `GlobalExceptionHandler` (@RestControllerAdvice) — xử lý lỗi tập trung
- `TaskSpecification` — JPA Criteria API, dynamic filter theo status/priority
- `TaskOverdueScheduler` — @Scheduled cron tự động cập nhật task quá hạn
- `WebMvcConfig` — CORS cho localhost:4200 và kickoff-fe-an.vercel.app
- **Lưu ý**: có 2 class ApiResponse khác nhau (`common.ApiResponse` cho Task, `dto.response.ApiResponse` cho User) — cần refactor về 1

### Entity hiện tại
```
users:  id(SERIAL) | name | email(UNIQUE) | role | created_at
tasks:  id(SERIAL) | title | description | status(ENUM) | priority(ENUM)
        | due_date | due_time | user_id(FK) | created_at | updated_at
        FK: tasks.user_id → users.id ON DELETE RESTRICT
```

### API hiện tại
```
GET/POST        /api/users
GET/PUT/DELETE  /api/users/:id
GET/POST        /api/tasks          (GET hỗ trợ ?status=&priority= qua Specification)
GET/PUT/DELETE  /api/tasks/:id
```

---

## CẤU TRÚC FRONTEND HIỆN TẠI

```
app.ts (shell: sidebar + header + router-outlet)
├── pages/
│   ├── task-list/     — danh sách task + filter + CRUD modal
│   ├── task-detail/   — chi tiết task, đọc :id từ route
│   └── user-list/     — danh sách user + CRUD modal
├── core/
│   ├── models/        — Task, User, ApiResponse interfaces
│   └── services/      — TaskService, UserService (HttpClient + RxJS map)
└── shared/navbar/
```

**Pattern quan trọng:**
- `ChangeDetectionStrategy.OnPush` + `cdr.markForCheck()` sau mỗi HTTP call
- Services dùng `.pipe(map(res => res.data))` để unwrap ApiResponse wrapper
- Lazy loading: `loadComponent: () => import(...)` cho mọi route
- `ngOnDestroy` + `unsubscribe()` tránh memory leak khi subscribe router.events
- `environment.apiUrl = ''` (empty) → Vercel proxy tự route /api/* sang Railway

---

## MODULE MỚI: THÔNG BÁO TÌNH HÌNH (cần implement)

### Mô tả
Hệ thống quản lý thông báo an ninh quân sự từ Cục BVANQĐ xuống các đơn vị. Module CORE, tần suất cao nhất.

### 3 Role & phân quyền
| Role | Quyền | Phạm vi dữ liệu |
|------|-------|-----------------|
| CBCT (Cán bộ chuyên trách) | CRUD + Import + Export | Đơn vị được phân quyền |
| Trưởng phòng | Xem + Search + Export + Xem audit | Toàn Cục |
| Thủ trưởng | Xem + Search (read-only) | Toàn ngành |

**Rule phân quyền**: Backend filter `WHERE don_vi_id IN (danh_sach_don_vi_cua_user)` dựa vào token. CBCT không xem/sửa được thông báo đơn vị khác dù biết ID.

### Data Model

**Bảng chính: `thong_bao_tinh_hinh`**
```sql
id              UUID PRIMARY KEY          -- UUID, không dùng SERIAL
tieu_de         VARCHAR(255) NOT NULL
phan_loai_id    INT FK → danh_muc_phan_loai
don_vi_id       INT FK NOT NULL
pham_vi         ENUM('TOAN_NGANH','NOI_BO_CUC') NOT NULL
ngay_thong_bao  DATE NOT NULL             -- không > hôm nay + 7 ngày
noi_dung        TEXT NOT NULL             -- HTML từ rich text editor
ghi_chu         TEXT NULL                 -- max 500 ký tự
is_deleted      BOOLEAN DEFAULT false     -- SOFT DELETE
created_by      INT FK NOT NULL
created_at      TIMESTAMP NOT NULL
updated_by      INT FK NULL
updated_at      TIMESTAMP NULL
```

**Bảng audit: `thong_bao_tinh_hinh_audit_log`**
```sql
id          PK | record_id FK | changed_by FK | changed_at TIMESTAMP
action      ENUM('CREATE','UPDATE','DELETE')
field_name  VARCHAR | old_value TEXT | new_value TEXT
```

**Bảng file: `thong_bao_file`**
```sql
id | thong_bao_id FK | file_name | file_path | file_size BIGINT
mime_type | uploaded_by FK | uploaded_at
-- Validate: max 5 file, mỗi file ≤ 10MB, chỉ .pdf .docx .xlsx .jpg .png
```

### API Endpoints
```
GET    /api/thong-bao-tinh-hinh              — list, query: page,limit,sort,search,phan_loai,pham_vi,from_date,to_date,don_vi_ids[]
GET    /api/thong-bao-tinh-hinh/:id          — chi tiết + files
POST   /api/thong-bao-tinh-hinh              — tạo mới, multipart/form-data
PUT    /api/thong-bao-tinh-hinh/:id          — cập nhật + ghi audit log
DELETE /api/thong-bao-tinh-hinh/:id          — soft delete (SET is_deleted=true)
GET    /api/thong-bao-tinh-hinh/:id/audit    — lịch sử chỉnh sửa
POST   /api/thong-bao-tinh-hinh/import/validate  — bước 1: validate Excel từng dòng
POST   /api/thong-bao-tinh-hinh/import/confirm   — bước 2: import dòng hợp lệ
GET    /api/thong-bao-tinh-hinh/template     — download file Excel mẫu
GET    /api/thong-bao-tinh-hinh/export       — xuất danh sách ra Excel
```

### Quy tắc nghiệp vụ bắt buộc
1. **Soft delete**: KHÔNG `DELETE FROM db`. Chỉ `SET is_deleted = true`. Dùng `@SQLDelete` + `@Where(clause="is_deleted=false")` ở Entity.
2. **Audit log**: Mỗi UPDATE phải ghi từng field thay đổi: field_name, old_value → new_value, changed_by, changed_at. Gọi `AuditLogService` trong ServiceImpl sau khi save.
3. **Ownership check**: Trước khi UPDATE/DELETE, backend verify `thongBao.donViId == currentUser.donViId`, ném `ForbiddenException` nếu sai.
4. **Double validate**: FE validate để UX tốt, BE validate lại để bảo mật — không tin client.
5. **Pagination**: GET list phải dùng `Pageable`, default 20/page, sort ngày mới nhất lên đầu.

### Những thứ cần thêm vào BE (so với code Kickoff hiện tại)
- `@SQLDelete` + `@Where` annotation cho soft delete
- `@GeneratedValue(strategy = GenerationType.UUID)` thay SERIAL
- `@CreatedBy`, `@LastModifiedBy` — Spring Data Auditing (cần `AuditorAware`)
- `AuditLogService` + `AuditLogRepository` (tạo mới)
- `Page<T>` + `Pageable` cho GET list thay vì `List<T>`
- `@RequestPart` + `MultipartFile` cho file upload (thay `@RequestBody`)
- dependency: `poi-ooxml` cho Excel import/export
- `application.properties`: `spring.servlet.multipart.max-file-size=10MB`

### Những thứ cần thêm vào FE (so với code Kickoff hiện tại)
- Rich text editor component (Quill hoặc TipTap)
- File upload component: validate size/mime ngay khi chọn, trước submit
- `debounceTime(300)` + `distinctUntilChanged()` trên search input (RxJS)
- Pagination component (20/50/100 per page)
- Role-based UI: `@if(currentUser.role === 'CBCT')` để ẩn/hiện nút CRUD
- Import 2-step component: bảng preview xanh/đỏ validate + inline edit

### Use Cases ưu tiên
- 🔴 Làm trước: UC01 (danh sách), UC02 (search nhanh), UC04 (chi tiết), UC05 (thêm), UC06 (sửa), UC07 (xóa soft)
- 🟠 Làm sau: UC03 (search nâng cao), UC08-09 (import Excel), UC10 (export)
- 🟡 Nice-to-have: UC11 (xem lịch sử audit)

### Definition of Done (sprint xong khi)
- [ ] CRUD đúng với 3 role, nút ẩn/hiện đúng theo role
- [ ] Search nhanh + nâng cao < 1s với 1.000 records
- [ ] Soft delete: không mất DB, không hiện ở danh sách
- [ ] Import: validate lỗi từng dòng, mô tả đến từng cell
- [ ] Audit log tự ghi đầy đủ mỗi thay đổi
- [ ] File upload: validate size + định dạng ngay khi chọn
- [ ] Đủ empty state, error state, loading state ở tất cả màn hình
