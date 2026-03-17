# Kiểm tra Database - Fitness Club Membership Portal

Chạy các lệnh sau trong **MySQL Workbench** (đã chọn database `fitness_club`) để kiểm tra bảng và dữ liệu.

---

## 1. Kiểm tra cấu trúc bảng (phải khớp với app)

### Bảng `membership_enrollments`
```sql
DESCRIBE membership_enrollments;
```
Cần có các cột: `id`, `member_id`, `plan_type`, `primary_branch_id`, `start_date`, `contract_duration`, `billing_type`, `plan_base_amount`, `total_amount`, `status`, `contract_pdf_path`, `created_at`.

### Bảng `enrollment_addons`
```sql
DESCRIBE enrollment_addons;
```
Cần có: `id`, `enrollment_id`, `addon_type`, `quantity`, `unit_price`.

### Bảng `members`
```sql
DESCRIBE members;
```
Cần có: `id`, `first_name`, `last_name`, `dob`, `health_goals`.

### Bảng `branches`
```sql
DESCRIBE branches;
```
Cần có: `id`, `name` (và `city` nếu app đã thêm).

---

## 2. Xem dữ liệu đăng ký (sau khi bấm "Gửi đăng ký")

```sql
SELECT id, member_id, plan_type, primary_branch_id, start_date, status, created_at 
FROM membership_enrollments 
ORDER BY id DESC 
LIMIT 10;
```

Nếu có hàng mới sau mỗi lần đăng ký → dữ liệu đã ghi đúng. Ghi lại **id** (ví dụ 9) và thử mở:  
`http://localhost:8081/enroll/result/9`

---

## 3. Kiểm tra từ app (sau khi chạy ứng dụng)

Mở trình duyệt:
- **http://localhost:8081/api/enrollments/ids**  
  → Trả về JSON danh sách id, ví dụ `[1,2,3,9]`.  
  → Nếu có id 9 nghĩa là app **đọc được** bảng `membership_enrollments`.

---

## 4. Nếu cột không khớp (DESCRIBE khác với trên)

Xóa bảng do Hibernate tạo sai rồi tạo lại bằng script:

```sql
-- Chỉ chạy khi cần tạo lại bảng (sẽ mất dữ liệu trong 2 bảng này)
DROP TABLE IF EXISTS enrollment_addons;
DROP TABLE IF EXISTS membership_enrollments;

-- Sau đó chạy lại nội dung file: src/main/resources/db/schema-app-tables.sql
```

Sau đó **restart** ứng dụng Spring Boot và thử đăng ký lại.
