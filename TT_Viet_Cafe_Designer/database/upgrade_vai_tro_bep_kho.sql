USE tt_viet_cafe;

-- T&T Viet: mở rộng tài khoản, bếp và kho. Có thể chạy lại an toàn, không xóa dữ liệu.
DROP PROCEDURE IF EXISTS migrate_vai_tro_bep_kho;
DELIMITER //
CREATE PROCEDURE migrate_vai_tro_bep_kho()
BEGIN
 IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='employee_id') THEN
  ALTER TABLE users ADD COLUMN employee_id INT NULL;
 END IF;
 IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND INDEX_NAME='uq_users_employee') THEN
  ALTER TABLE users ADD UNIQUE KEY uq_users_employee (employee_id);
 END IF;
 IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_details' AND COLUMN_NAME='kitchen_status') THEN
  ALTER TABLE order_details ADD COLUMN kitchen_status VARCHAR(30) NOT NULL DEFAULT 'Chờ chế biến';
 END IF;
 IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_details' AND COLUMN_NAME='note') THEN
  ALTER TABLE order_details ADD COLUMN note VARCHAR(255) NULL;
 END IF;
END//
DELIMITER ;
CALL migrate_vai_tro_bep_kho();
DROP PROCEDURE migrate_vai_tro_bep_kho;

CREATE TABLE IF NOT EXISTS inventory_items (
 id INT AUTO_INCREMENT PRIMARY KEY,
 item_name VARCHAR(150) NOT NULL UNIQUE,
 unit_name VARCHAR(30) NOT NULL DEFAULT 'kg',
 quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
 min_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
 active TINYINT NOT NULL DEFAULT 1,
 created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventory_movements (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 item_id INT NOT NULL,
 movement_type ENUM('NHAP','XUAT','DIEU_CHINH') NOT NULL,
 quantity DECIMAL(12,3) NOT NULL,
 note VARCHAR(255),
 created_by INT NULL,
 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_inventory_movement_item FOREIGN KEY(item_id) REFERENCES inventory_items(id),
 CONSTRAINT fk_inventory_movement_user FOREIGN KEY(created_by) REFERENCES users(id)
) ENGINE=InnoDB;
