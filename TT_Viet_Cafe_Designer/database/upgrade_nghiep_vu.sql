USE tt_viet_cafe;

CREATE TABLE IF NOT EXISTS employees(
 id INT AUTO_INCREMENT PRIMARY KEY,
 employee_code VARCHAR(20) UNIQUE,
 full_name VARCHAR(120) NOT NULL,
 phone VARCHAR(25),
 position_name VARCHAR(50) NOT NULL DEFAULT 'Phục vụ',
 active TINYINT NOT NULL DEFAULT 1,
 created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS work_shifts(
 id INT AUTO_INCREMENT PRIMARY KEY,
 employee_id INT NOT NULL,
 shift_date DATE NOT NULL,
 shift_name VARCHAR(30) NOT NULL,
 start_time TIME,
 end_time TIME,
 note VARCHAR(255),
 CONSTRAINT fk_shift_employee FOREIGN KEY(employee_id) REFERENCES employees(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS asset_compensations(
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 description VARCHAR(255) NOT NULL,
 quantity INT NOT NULL DEFAULT 1,
 unit_cost DECIMAL(12,2) NOT NULL,
 amount DECIMAL(12,2) NOT NULL,
 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
 note VARCHAR(255)
) ENGINE=InnoDB;

ALTER TABLE cafe_tables ADD COLUMN zone_name VARCHAR(50) NOT NULL DEFAULT 'Khu vực chung';

INSERT IGNORE INTO cafe_tables(table_name,zone_name,status) VALUES
('Bàn 05','Trong nhà','Trống'),('Bàn 06','Trong nhà','Trống'),
('Bàn 07','Ngoài trời','Trống'),('Bàn 08','Ngoài trời','Trống'),
('Bàn 09','Phòng lạnh','Trống'),('Bàn 10','Phòng lạnh','Trống');

-- Tài khoản mẫu: admin / 123456 (dữ liệu đăng nhập theo project hiện tại)
