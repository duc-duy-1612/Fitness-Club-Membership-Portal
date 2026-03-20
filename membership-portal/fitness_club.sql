-- 1. Tạo Database và sử dụng nó
CREATE DATABASE IF NOT EXISTS fitness_club;
USE fitness_club;

-- 2. Bảng Thành viên (Member)
CREATE TABLE members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE NOT NULL,
    health_goals TEXT
);

-- 3. Bảng Chi nhánh (Branch - Hệ thống có 5 chi nhánh)
CREATE TABLE branches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NULL
);

-- 3.1 Bảng người dùng (users) cho Spring Security
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Seed user admin (password sẽ được mã hóa BCrypt khi chạy app do initializer)
INSERT INTO users (username, password, role)
VALUES ('admin', 'admin123', 'ADMIN')
ON DUPLICATE KEY UPDATE password = VALUES(password), role = VALUES(role);

-- 4. Bảng Gói tập (Plan - Basic / Premium)
CREATE TABLE plans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    plan_name VARCHAR(50) NOT NULL,
    price_per_month DECIMAL(10, 2) NOT NULL,
    description TEXT
);

-- 5. Bảng Dịch vụ phụ (Addon Services - PT, Locker)
CREATE TABLE addon_services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- 6. Bảng Hợp đồng / Thông tin đăng ký (Contract)
CREATE TABLE contracts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    plan_id INT NOT NULL,
    primary_branch_id INT, -- Có thể NULL nếu là gói Premium (được tập toàn hệ thống)
    start_date DATE NOT NULL,
    duration_months INT NOT NULL, -- Thời hạn: 1, 6, hoặc 12 tháng
    payment_type ENUM('MONTHLY', 'UPFRONT') NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'SIGNED', 'ACTIVE') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (plan_id) REFERENCES plans(id),
    FOREIGN KEY (primary_branch_id) REFERENCES branches(id)
);

-- 7. Bảng Chi tiết dịch vụ phụ trong hợp đồng (Contract_Addons)
CREATE TABLE contract_addons (
    contract_id INT NOT NULL,
    addon_id INT NOT NULL,
    quantity INT DEFAULT 1,
    PRIMARY KEY (contract_id, addon_id),
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    FOREIGN KEY (addon_id) REFERENCES addon_services(id)
);

-- 1. Bổ sung thêm một vài Dịch vụ phụ (Addons) cho phong phú
INSERT INTO addon_services (service_name, price) VALUES 
('Yoga Class (Monthly)', 200000.00),
('Nutrition Consultation (1 Session)', 500000.00);

-- 2. Thêm 15 Thành viên mẫu với các mục tiêu sức khỏe khác nhau
INSERT INTO members (first_name, last_name, dob, health_goals) VALUES
('Duy', 'Nguyễn', '2002-05-15', 'Giảm cân, rèn luyện thể lực và tăng cơ'),
('Thảo', 'Trần', '2002-10-20', 'Giữ dáng, tập yoga nhẹ nhàng'),
('Tuấn', 'Lê', '1995-02-28', 'Tăng cơ bắp, tập tạ nặng'),
('Hoa', 'Phạm', '1998-11-05', 'Cải thiện sức bền tim mạch'),
('Minh', 'Hoàng', '2000-07-12', 'Giảm mỡ bụng, siết cơ'),
('Lan', 'Đỗ', '1993-04-18', 'Tập phục hồi cơ bản'),
('Hải', 'Vũ', '1990-09-25', 'Tăng cường sức khỏe tổng thể'),
('Nhung', 'Đặng', '2001-12-30', 'Tập bơi và sử dụng sauna thư giãn'),
('Thành', 'Bùi', '1988-08-08', 'Giảm cân cấp tốc'),
('Oanh', 'Ngô', '1997-03-14', 'Chuẩn bị thể lực cho giải chạy marathon'),
('Đạt', 'Lý', '1999-06-22', 'Tăng cân và tăng lượng cơ nạc'),
('Mai', 'Hồ', '1996-01-10', 'Yoga và pilates định hình cơ thể'),
('Khánh', 'Châu', '2003-05-05', 'Tập gym cơ bản cho người mới'),
('Trang', 'Dương', '1994-10-11', 'Giảm stress sau giờ làm, rèn luyện sức khỏe'),
('Sơn', 'Đinh', '1991-07-19', 'Tập luyện cường độ cao (HIIT)');

-- 3. Thêm 10 Hợp đồng (Contracts) đa dạng các trường hợp
-- Lưu ý logic: Gói Basic (1) cần chọn chi nhánh (primary_branch_id). Gói Premium (2) tập toàn hệ thống nên chi nhánh = NULL.
INSERT INTO contracts (member_id, plan_id, primary_branch_id, start_date, duration_months, payment_type, total_amount, status) VALUES
(1, 1, 1, '2026-03-01', 6, 'UPFRONT', 3000000.00, 'ACTIVE'),   -- Duy: Gói Basic, 6 tháng, trả trước, Đang hoạt động
(2, 2, NULL, '2026-03-05', 12, 'MONTHLY', 10800000.00, 'ACTIVE'), -- Thảo: Gói Premium, 1 năm, trả hàng tháng, Đang hoạt động
(3, 1, 3, '2026-03-10', 1, 'MONTHLY', 500000.00, 'SIGNED'),     -- Tuấn: Basic, 1 tháng, Đã ký hợp đồng
(4, 2, NULL, '2026-03-15', 6, 'UPFRONT', 5400000.00, 'PENDING'),  -- Hoa: Premium, 6 tháng, Đang chờ duyệt
(5, 1, 2, '2026-02-01', 12, 'UPFRONT', 6000000.00, 'ACTIVE'),   -- Minh: Basic, 1 năm
(6, 1, 4, '2026-03-11', 1, 'MONTHLY', 500000.00, 'PENDING'),    -- Lan: Basic, 1 tháng, Đang chờ
(7, 2, NULL, '2026-01-15', 6, 'MONTHLY', 5400000.00, 'ACTIVE'),   -- Hải: Premium, 6 tháng
(8, 2, NULL, '2026-03-20', 1, 'UPFRONT', 900000.00, 'SIGNED'),    -- Nhung: Premium, 1 tháng
(9, 1, 5, '2025-12-01', 12, 'MONTHLY', 6000000.00, 'ACTIVE'),   -- Thành: Basic, 1 năm
(10, 1, 1, '2026-03-12', 6, 'UPFRONT', 3000000.00, 'PENDING');  -- Oanh: Basic, 6 tháng

-- 4. Thêm các Dịch vụ phụ đi kèm vào Hợp đồng (Enrollment Cart test)
-- Addon IDs đang có: 1 (PT 300k), 2 (Locker 100k), 3 (Yoga 200k), 4 (Nutrition 500k)
INSERT INTO contract_addons (contract_id, addon_id, quantity) VALUES
(1, 2, 6),   -- Duy thuê tủ đồ (Locker) trong 6 tháng
(1, 4, 1),   -- Duy mua 1 buổi tư vấn dinh dưỡng
(2, 3, 12),  -- Thảo đăng ký lớp Yoga 12 tháng
(3, 1, 5),   -- Tuấn thuê PT 5 buổi
(4, 2, 6),   -- Hoa thuê tủ đồ 6 tháng
(5, 1, 10),  -- Minh thuê PT 10 buổi
(7, 4, 1),   -- Hải tư vấn dinh dưỡng 1 buổi
(8, 2, 1),   -- Nhung thuê tủ đồ 1 tháng
(9, 3, 12),  -- Thành đăng ký Yoga 12 tháng
(10, 1, 2);  -- Oanh thuê PT 2 buổi

-- Thêm 5 chi nhánh mẫu
INSERT INTO branches (name) VALUES 
('Chi nhánh Quận 1 - Center'),
('Chi nhánh Quận 3 - Premium'),
('Chi nhánh Quận 7 - Sunrise'),
('Chi nhánh Tân Bình - Airport'),
('Chi nhánh Thủ Đức - University');

-- Thêm 2 gói tập chính
INSERT INTO plans (plan_name, price_per_month, description) VALUES 
('Basic', 500000.00, 'Chỉ sử dụng phòng Gym tại 1 chi nhánh đăng ký.'),
('Premium', 900000.00, 'Sử dụng Gym, Hồ bơi, Sauna tại toàn bộ 5 chi nhánh.');

-- Thêm dịch vụ phụ cơ bản
INSERT INTO addon_services (service_name, price) VALUES 
('Personal Training (1 Session)', 300000.00),
('Locker Rental (Monthly)', 100000.00);

USE fitness_club;

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

CREATE TABLE IF NOT EXISTS enrollment_addons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    addon_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    FOREIGN KEY (enrollment_id) REFERENCES membership_enrollments(id) ON DELETE CASCADE
);

-- ==========================
-- Seed dữ liệu app: membership_enrollments + enrollment_addons
-- (phục vụ các endpoint GET /api/enrollments/{id} và /api/enrollments/ids)
-- ==========================
INSERT INTO membership_enrollments (
    id, member_id, plan_type, primary_branch_id, start_date,
    contract_duration, billing_type, plan_base_amount, total_amount,
    status, contract_pdf_path, created_at
) VALUES
    (1, 1, 'BASIC', 1, '2026-03-01', 'SIX_MONTH', 'ONE_TIME_UPFRONT', 3000000.00, 3700000.00, 'DRAFT', NULL, NOW()),
    (2, 2, 'PREMIUM', NULL, '2026-03-02', 'MONTHLY', 'MONTHLY', 900000.00, 1200000.00, 'DRAFT', NULL, NOW()),
    (3, 3, 'BASIC', 2, '2026-03-03', 'MONTHLY', 'ONE_TIME_UPFRONT', 500000.00, 900000.00, 'DRAFT', NULL, NOW()),
    (4, 4, 'PREMIUM', NULL, '2026-03-04', 'SIX_MONTH', 'ONE_TIME_UPFRONT', 5400000.00, 5500000.00, 'DRAFT', NULL, NOW()),
    (5, 5, 'BASIC', 3, '2026-03-05', 'ANNUAL', 'MONTHLY', 6000000.00, 3500000.00, 'DRAFT', NULL, NOW()),
    (6, 6, 'PREMIUM', NULL, '2026-03-06', 'ANNUAL', 'ONE_TIME_UPFRONT', 10800000.00, 10900000.00, 'DRAFT', NULL, NOW())
ON DUPLICATE KEY UPDATE
    member_id = VALUES(member_id),
    plan_type = VALUES(plan_type),
    primary_branch_id = VALUES(primary_branch_id),
    start_date = VALUES(start_date),
    contract_duration = VALUES(contract_duration),
    billing_type = VALUES(billing_type),
    plan_base_amount = VALUES(plan_base_amount),
    total_amount = VALUES(total_amount),
    status = VALUES(status),
    contract_pdf_path = VALUES(contract_pdf_path),
    created_at = VALUES(created_at);

INSERT INTO enrollment_addons (id, enrollment_id, addon_type, quantity, unit_price) VALUES
    -- Enrollment 1: Locker 6 tháng + PT 2 buổi
    (1, 1, 'LOCKER_RENTAL', 1, 100000.00),
    (2, 1, 'PERSONAL_TRAINING', 2, 300000.00),
    -- Enrollment 2: PT 3 buổi
    (3, 2, 'PERSONAL_TRAINING', 1, 300000.00),
    -- Enrollment 3: PT 5 buổi + Locker 1 tháng
    (4, 3, 'PERSONAL_TRAINING', 1, 300000.00),
    (5, 3, 'LOCKER_RENTAL', 1, 100000.00),
    -- Enrollment 4: Locker 6 tháng
    (6, 4, 'LOCKER_RENTAL', 1, 100000.00),
    -- Enrollment 5: PT 10 buổi
    (7, 5, 'PERSONAL_TRAINING', 10, 300000.00),
    -- Enrollment 6: Locker 12 tháng
    (8, 6, 'LOCKER_RENTAL', 1, 100000.00)
ON DUPLICATE KEY UPDATE
    enrollment_id = VALUES(enrollment_id),
    addon_type = VALUES(addon_type),
    quantity = VALUES(quantity),
    unit_price = VALUES(unit_price);

DESCRIBE membership_enrollments;

SELECT id, member_id, plan_type, start_date, status 
FROM membership_enrollments 
ORDER BY id DESC 
LIMIT 5;
