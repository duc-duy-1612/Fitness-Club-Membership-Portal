# Kiểm tra kết nối MySQL và dữ liệu

## 1. Cấu hình hiện tại (application.properties)

- **URL:** `jdbc:mysql://localhost:3306/fitness_club`
- **User:** root  
- **Port:** 3306 (mặc định MySQL)

Cần đảm bảo:
- MySQL đang chạy trên **cùng máy** (localhost).
- Có **database tên đúng** `fitness_club` (chữ thường, có gạch dưới).
- User `root` với mật khẩu đúng (trong file là `123456`).

---

## 2. Kiểm tra bằng ứng dụng (sau khi chạy app)

Mở trình duyệt:

```
http://localhost:8080/api/enrollments/db-check
```

Bạn sẽ thấy:
- `datasourceUrl`: đường dẫn DB app đang dùng (mật khẩu đã bị ẩn).
- `enrollmentIds_from_app`: danh sách id đăng ký mà **app đọc được** từ bảng `membership_enrollments`.

**So sánh với MySQL Workbench:**
- Trong Workbench, chọn database `fitness_club`, chạy:
  ```sql
  SELECT id FROM membership_enrollments ORDER BY id DESC;
  ```
- Nếu danh sách id trong Workbench **giống** `enrollmentIds_from_app` → app đang nối đúng DB, lỗi có thể do phần đọc chi tiết (JOIN members, v.v.).
- Nếu `enrollmentIds_from_app` **rỗng** hoặc **khác** → app đang nối **sai database** hoặc sai server/port (ví dụ một bên dùng 3306, một bên dùng 3307).

---

## 3. Nếu nghi ngờ sai database / sai kết nối

- Kiểm tra trong Workbench: **Server → Data Export** hoặc **Schemas**: tên database có đúng là `fitness_club` không?
- Nếu bạn tạo database với tên khác (ví dụ `FitnessClub`, `fitnessclub`), sửa trong `application.properties`:
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/TÊN_DATABASE_ĐÚNG?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  ```
- Đổi port (ví dụ 3307): thêm port vào URL:
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3307/fitness_club?...
  ```

---

## 4. Sau khi sửa cấu hình

- Lưu file, **restart** ứng dụng.
- Gửi form đăng ký lại, mở `/enroll/result/{id}`.
- Nếu vẫn lỗi, mở lại `/api/enrollments/db-check` và so sánh `enrollmentIds_from_app` với Workbench như bước 2.
