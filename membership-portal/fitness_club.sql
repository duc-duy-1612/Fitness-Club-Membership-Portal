-- ============================================================
-- Fitness Club Membership Portal - Submission SQL (DDL + DML)
-- Scope: tables used by Spring Boot entities/controllers in this project
-- ============================================================

CREATE DATABASE IF NOT EXISTS fitness_club;
USE fitness_club;

-- Optional reset for clean testing
DROP TABLE IF EXISTS enrollment_addons;
DROP TABLE IF EXISTS membership_enrollments;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS branches;
DROP TABLE IF EXISTS members;

-- 1) Members
CREATE TABLE members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE NOT NULL,
    health_goals TEXT NULL,
    email VARCHAR(120) NULL
);

-- 2) Branches (5 city branches)
CREATE TABLE branches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NULL
);

-- 3) Users for Spring Security
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    member_id INT NULL,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE SET NULL
);

-- Note:
-- Password below starts as plain text; UserAccountInitializer converts to BCrypt on app startup.
-- Default admin account used for demo
INSERT INTO users (username, password, role)
VALUES ('admin@gmail.com', 'admin123', 'ADMIN') AS new
ON DUPLICATE KEY UPDATE password = new.password, role = new.role;

-- 4) Enrollment header
CREATE TABLE membership_enrollments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    primary_branch_id INT NULL,
    start_date DATE NOT NULL,
    contract_duration VARCHAR(20) NOT NULL,
    billing_type VARCHAR(20) NOT NULL,
    plan_base_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    contract_pdf_path VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (primary_branch_id) REFERENCES branches(id) ON DELETE SET NULL
);

-- 5) Enrollment add-ons
CREATE TABLE enrollment_addons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    addon_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (enrollment_id) REFERENCES membership_enrollments(id) ON DELETE CASCADE
);

-- ----------------------------
-- DML mock data (>= 5 records)
-- ----------------------------

-- Members (10)
INSERT INTO members (id, first_name, last_name, dob, health_goals) VALUES
(1, 'Dinh', 'Nghia', '2003-06-01', 'Giảm cân'),
(2, 'Minh', 'Anh', '2001-02-14', 'Tăng cơ'),
(3, 'Quang', 'Huy', '1999-09-20', 'Sức bền'),
(4, 'Thanh', 'Truc', '2000-11-11', 'Giữ dáng'),
(5, 'Bao', 'Tran', '1998-04-05', 'Phục hồi vận động'),
(6, 'Khanh', 'Ly', '2002-12-22', 'Tăng thể lực'),
(7, 'Gia', 'Bao', '1997-07-30', 'Giảm mỡ'),
(8, 'Thao', 'Le', '2004-01-18', 'Tập bơi'),
(9, 'Hai', 'Nam', '1996-03-12', 'HIIT'),
(10, 'Lan', 'Nguyen', '2003-08-08', 'Sức khỏe tổng quát')
ON DUPLICATE KEY UPDATE
first_name = VALUES(first_name), last_name = VALUES(last_name), dob = VALUES(dob), health_goals = VALUES(health_goals);

-- Branches (5)
INSERT INTO branches (id, name, city) VALUES
(1, 'Chi nhánh Quận 1 - Center', 'Ho Chi Minh City'),
(2, 'Chi nhánh Quận 3 - Premium', 'Ho Chi Minh City'),
(3, 'Chi nhánh Quận 7 - Sunrise', 'Ho Chi Minh City'),
(4, 'Chi nhánh Tân Bình - Airport', 'Ho Chi Minh City'),
(5, 'Chi nhánh Thủ Đức - University', 'Ho Chi Minh City')
ON DUPLICATE KEY UPDATE name = VALUES(name), city = VALUES(city);

-- Enrollments (10)
INSERT INTO membership_enrollments (
    id, member_id, plan_type, primary_branch_id, start_date,
    contract_duration, billing_type, plan_base_amount, total_amount,
    status, contract_pdf_path, created_at
) VALUES
(1, 1, 'BASIC', 1, '2026-06-01', 'MONTHLY', 'MONTHLY', 500000.00, 500000.00, 'DRAFT', NULL, NOW()),
(2, 2, 'PREMIUM', NULL, '2026-06-02', 'SIX_MONTH', 'ONE_TIME_UPFRONT', 5400000.00, 7200000.00, 'DRAFT', NULL, NOW()),
(3, 3, 'BASIC', 2, '2026-06-03', 'ANNUAL', 'ONE_TIME_UPFRONT', 6000000.00, 6300000.00, 'DRAFT', NULL, NOW()),
(4, 4, 'PREMIUM', NULL, '2026-06-04', 'MONTHLY', 'MONTHLY', 900000.00, 1300000.00, 'DRAFT', NULL, NOW()),
(5, 5, 'BASIC', 3, '2026-06-05', 'SIX_MONTH', 'MONTHLY', 3000000.00, 900000.00, 'DRAFT', NULL, NOW()),
(6, 6, 'PREMIUM', NULL, '2026-06-06', 'ANNUAL', 'MONTHLY', 10800000.00, 1900000.00, 'FINALIZED', 'C:/temp/contract-6.pdf', NOW()),
(7, 7, 'BASIC', 4, '2026-06-07', 'SIX_MONTH', 'ONE_TIME_UPFRONT', 3000000.00, 4800000.00, 'DRAFT', NULL, NOW()),
(8, 8, 'PREMIUM', NULL, '2026-06-08', 'MONTHLY', 'ONE_TIME_UPFRONT', 900000.00, 1300000.00, 'DRAFT', NULL, NOW()),
(9, 9, 'BASIC', 5, '2026-06-09', 'ANNUAL', 'ONE_TIME_UPFRONT', 6000000.00, 7200000.00, 'DRAFT', NULL, NOW()),
(10, 10, 'PREMIUM', NULL, '2026-06-10', 'SIX_MONTH', 'MONTHLY', 5400000.00, 1000000.00, 'DRAFT', NULL, NOW())
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

-- Add-ons (consistent with pricing constants: PT 300000, LOCKER 100000)
INSERT INTO enrollment_addons (id, enrollment_id, addon_type, quantity, unit_price) VALUES
(1, 2, 'PERSONAL_TRAINING', 6, 300000.00),
(2, 3, 'LOCKER_RENTAL', 3, 100000.00),
(3, 4, 'PERSONAL_TRAINING', 60, 300000.00), -- MONTHLY billing: app displays/charges max 30
(4, 4, 'LOCKER_RENTAL', 4, 100000.00),      -- MONTHLY billing: app displays/charges 0/1
(5, 5, 'PERSONAL_TRAINING', 1, 300000.00),
(6, 6, 'PERSONAL_TRAINING', 30, 300000.00),
(7, 7, 'LOCKER_RENTAL', 6, 100000.00),
(8, 7, 'PERSONAL_TRAINING', 6, 300000.00),
(9, 8, 'LOCKER_RENTAL', 4, 100000.00),
(10, 9, 'LOCKER_RENTAL', 12, 100000.00),
(11, 10, 'PERSONAL_TRAINING', 30, 300000.00)
ON DUPLICATE KEY UPDATE
enrollment_id = VALUES(enrollment_id),
addon_type = VALUES(addon_type),
quantity = VALUES(quantity),
unit_price = VALUES(unit_price);

-- Quick verification queries
SELECT id, username, role, password FROM users WHERE username = 'admin';
SELECT id, first_name, last_name FROM members ORDER BY id;
SELECT id, name, city FROM branches ORDER BY id;
SELECT id, plan_type, contract_duration, billing_type, total_amount, status FROM membership_enrollments ORDER BY id;
SELECT id, enrollment_id, addon_type, quantity, unit_price FROM enrollment_addons ORDER BY id;

-- ============================================================
-- Fitness Club Membership Portal - Submission SQL (DDL + DML)
-- Scope: tables used by Spring Boot entities/controllers in this project
-- ============================================================

CREATE DATABASE IF NOT EXISTS fitness_club;
USE fitness_club;

-- Optional reset for clean testing
DROP TABLE IF EXISTS enrollment_addons;
DROP TABLE IF EXISTS membership_enrollments;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS branches;
DROP TABLE IF EXISTS members;

-- 1) Members
CREATE TABLE members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE NOT NULL,
    health_goals TEXT NULL,
    email VARCHAR(120) NULL
);

-- 2) Branches (5 city branches)
CREATE TABLE branches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NULL
);

-- 3) Users for Spring Security
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    member_id INT NULL,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE SET NULL
);

-- Note:
-- Password below starts as plain text; UserAccountInitializer converts to BCrypt on app startup.
-- Default admin account used for demo
INSERT INTO users (username, password, role)
VALUES ('admin@gmail.com', 'admin123', 'ADMIN') AS new
ON DUPLICATE KEY UPDATE password = new.password, role = new.role;

-- 4) Enrollment header
CREATE TABLE membership_enrollments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    primary_branch_id INT NULL,
    start_date DATE NOT NULL,
    contract_duration VARCHAR(20) NOT NULL,
    billing_type VARCHAR(20) NOT NULL,
    plan_base_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    contract_pdf_path VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (primary_branch_id) REFERENCES branches(id) ON DELETE SET NULL
);

-- 5) Enrollment add-ons
CREATE TABLE enrollment_addons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    addon_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (enrollment_id) REFERENCES membership_enrollments(id) ON DELETE CASCADE
);

-- ----------------------------
-- DML mock data (>= 5 records)
-- ----------------------------

-- Members (10)
INSERT INTO members (id, first_name, last_name, dob, health_goals) VALUES
(1, 'Dinh', 'Nghia', '2003-06-01', 'Giảm cân'),
(2, 'Minh', 'Anh', '2001-02-14', 'Tăng cơ'),
(3, 'Quang', 'Huy', '1999-09-20', 'Sức bền'),
(4, 'Thanh', 'Truc', '2000-11-11', 'Giữ dáng'),
(5, 'Bao', 'Tran', '1998-04-05', 'Phục hồi vận động'),
(6, 'Khanh', 'Ly', '2002-12-22', 'Tăng thể lực'),
(7, 'Gia', 'Bao', '1997-07-30', 'Giảm mỡ'),
(8, 'Thao', 'Le', '2004-01-18', 'Tập bơi'),
(9, 'Hai', 'Nam', '1996-03-12', 'HIIT'),
(10, 'Lan', 'Nguyen', '2003-08-08', 'Sức khỏe tổng quát')
ON DUPLICATE KEY UPDATE
first_name = VALUES(first_name), last_name = VALUES(last_name), dob = VALUES(dob), health_goals = VALUES(health_goals);

-- Branches (5)
INSERT INTO branches (id, name, city) VALUES
(1, 'Chi nhánh Quận 1 - Center', 'Ho Chi Minh City'),
(2, 'Chi nhánh Quận 3 - Premium', 'Ho Chi Minh City'),
(3, 'Chi nhánh Quận 7 - Sunrise', 'Ho Chi Minh City'),
(4, 'Chi nhánh Tân Bình - Airport', 'Ho Chi Minh City'),
(5, 'Chi nhánh Thủ Đức - University', 'Ho Chi Minh City')
ON DUPLICATE KEY UPDATE name = VALUES(name), city = VALUES(city);

-- Enrollments (10)
INSERT INTO membership_enrollments (
    id, member_id, plan_type, primary_branch_id, start_date,
    contract_duration, billing_type, plan_base_amount, total_amount,
    status, contract_pdf_path, created_at
) VALUES
(1, 1, 'BASIC', 1, '2026-06-01', 'MONTHLY', 'MONTHLY', 500000.00, 500000.00, 'DRAFT', NULL, NOW()),
(2, 2, 'PREMIUM', NULL, '2026-06-02', 'SIX_MONTH', 'ONE_TIME_UPFRONT', 5400000.00, 7200000.00, 'DRAFT', NULL, NOW()),
(3, 3, 'BASIC', 2, '2026-06-03', 'ANNUAL', 'ONE_TIME_UPFRONT', 6000000.00, 6300000.00, 'DRAFT', NULL, NOW()),
(4, 4, 'PREMIUM', NULL, '2026-06-04', 'MONTHLY', 'MONTHLY', 900000.00, 1300000.00, 'DRAFT', NULL, NOW()),
(5, 5, 'BASIC', 3, '2026-06-05', 'SIX_MONTH', 'MONTHLY', 3000000.00, 900000.00, 'DRAFT', NULL, NOW()),
(6, 6, 'PREMIUM', NULL, '2026-06-06', 'ANNUAL', 'MONTHLY', 10800000.00, 1900000.00, 'FINALIZED', 'C:/temp/contract-6.pdf', NOW()),
(7, 7, 'BASIC', 4, '2026-06-07', 'SIX_MONTH', 'ONE_TIME_UPFRONT', 3000000.00, 4800000.00, 'DRAFT', NULL, NOW()),
(8, 8, 'PREMIUM', NULL, '2026-06-08', 'MONTHLY', 'ONE_TIME_UPFRONT', 900000.00, 1300000.00, 'DRAFT', NULL, NOW()),
(9, 9, 'BASIC', 5, '2026-06-09', 'ANNUAL', 'ONE_TIME_UPFRONT', 6000000.00, 7200000.00, 'DRAFT', NULL, NOW()),
(10, 10, 'PREMIUM', NULL, '2026-06-10', 'SIX_MONTH', 'MONTHLY', 5400000.00, 1000000.00, 'DRAFT', NULL, NOW())
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

-- Add-ons (consistent with pricing constants: PT 300000, LOCKER 100000)
INSERT INTO enrollment_addons (id, enrollment_id, addon_type, quantity, unit_price) VALUES
(1, 2, 'PERSONAL_TRAINING', 6, 300000.00),
(2, 3, 'LOCKER_RENTAL', 3, 100000.00),
(3, 4, 'PERSONAL_TRAINING', 60, 300000.00), -- MONTHLY billing: app displays/charges max 30
(4, 4, 'LOCKER_RENTAL', 4, 100000.00),      -- MONTHLY billing: app displays/charges 0/1
(5, 5, 'PERSONAL_TRAINING', 1, 300000.00),
(6, 6, 'PERSONAL_TRAINING', 30, 300000.00),
(7, 7, 'LOCKER_RENTAL', 6, 100000.00),
(8, 7, 'PERSONAL_TRAINING', 6, 300000.00),
(9, 8, 'LOCKER_RENTAL', 4, 100000.00),
(10, 9, 'LOCKER_RENTAL', 12, 100000.00),
(11, 10, 'PERSONAL_TRAINING', 30, 300000.00)
ON DUPLICATE KEY UPDATE
enrollment_id = VALUES(enrollment_id),
addon_type = VALUES(addon_type),
quantity = VALUES(quantity),
unit_price = VALUES(unit_price);

-- Quick verification queries
SELECT id, username, role, password FROM users WHERE username = 'admin';
SELECT id, first_name, last_name FROM members ORDER BY id;
SELECT id, name, city FROM branches ORDER BY id;
SELECT id, plan_type, contract_duration, billing_type, total_amount, status FROM membership_enrollments ORDER BY id;
SELECT id, enrollment_id, addon_type, quantity, unit_price FROM enrollment_addons ORDER BY id;

