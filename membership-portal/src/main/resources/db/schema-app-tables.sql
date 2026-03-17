-- ============================================================
-- Bảng dùng bởi ỨNG DỤNG SPRING BOOT (Fitness Club Membership Portal)
-- Chạy script này TRONG database fitness_club đã tạo.
-- Các bảng contracts, plans, addon_services của bạn giữ nguyên;
-- script này chỉ thêm 2 bảng mà app đăng ký form cần.
-- ============================================================

USE fitness_club;

-- (Tùy chọn) Nếu bảng branches chưa có cột city, chạy lệnh sau một lần:
-- ALTER TABLE branches ADD COLUMN city VARCHAR(50) NULL;

-- Bảng đăng ký gói (khi user bấm "Gửi đăng ký" từ form web)
CREATE TABLE IF NOT EXISTS membership_enrollments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    primary_branch_id INT NULL,
    start_date DATE NOT NULL,
    contract_duration VARCHAR(20) NOT NULL,
    billing_type VARCHAR(20) NOT NULL,
    plan_base_amount DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    contract_pdf_path VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (primary_branch_id) REFERENCES branches(id) ON DELETE SET NULL
);

-- Bảng dịch vụ thêm trong đăng ký (PT, Locker)
CREATE TABLE IF NOT EXISTS enrollment_addons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    addon_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    FOREIGN KEY (enrollment_id) REFERENCES membership_enrollments(id) ON DELETE CASCADE
);
