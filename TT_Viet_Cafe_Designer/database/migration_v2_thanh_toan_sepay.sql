-- T&T VIET - Migration nghiep vu v2
-- An toan: KHONG DROP/TRUNCATE, khong xoa/sua du lieu don hang cu.
-- Chay trong phpMyAdmin sau khi chon database tt_viet_cafe.
USE tt_viet_cafe;

-- Them cac cot moi theo cach co the chay lai nhieu lan.
SET @db_name = DATABASE();

SET @sql = (SELECT IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='orders' AND COLUMN_NAME='payment_status'),
  'SELECT 1',
  'ALTER TABLE orders ADD COLUMN payment_status VARCHAR(30) NOT NULL DEFAULT ''Chưa đối soát'''
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='orders' AND COLUMN_NAME='payment_method'),
  'SELECT 1',
  'ALTER TABLE orders ADD COLUMN payment_method VARCHAR(30) NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='orders' AND COLUMN_NAME='paid_at'),
  'SELECT 1',
  'ALTER TABLE orders ADD COLUMN paid_at DATETIME NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='orders' AND COLUMN_NAME='status'),
  'ALTER TABLE orders MODIFY COLUMN status VARCHAR(40) NOT NULL DEFAULT ''Đang phục vụ''',
  'SELECT 1'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='products' AND COLUMN_NAME='image_path'),
  'SELECT 1',
  'ALTER TABLE products ADD COLUMN image_path VARCHAR(255) NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- Ghi chu rieng cho tung mon (it duong, khong da, khong hanh...).
SET @sql = (SELECT IF(
  EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db_name AND TABLE_NAME='order_details' AND COLUMN_NAME='note'),
  'SELECT 1',
  'ALTER TABLE order_details ADD COLUMN note VARCHAR(500) NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Luu tung giao dich nhan duoc tu SePay. transaction_code la duy nhat de chong webhook lap.
CREATE TABLE IF NOT EXISTS payment_transactions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  provider VARCHAR(30) NOT NULL DEFAULT 'SEPAY',
  transaction_code VARCHAR(100) NOT NULL,
  order_code VARCHAR(50) NULL,
  amount DECIMAL(14,2) NOT NULL,
  transfer_content VARCHAR(255) NULL,
  transaction_time DATETIME NULL,
  raw_payload JSON NULL,
  verification_status VARCHAR(30) NOT NULL DEFAULT 'Chưa xác minh',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_payment_provider_transaction (provider, transaction_code),
  KEY idx_payment_order_code (order_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Nhat ky webhook: luu id su kien neu nha cung cap gui lai.
CREATE TABLE IF NOT EXISTS payment_webhook_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  provider VARCHAR(30) NOT NULL DEFAULT 'SEPAY',
  event_key VARCHAR(120) NULL,
  received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  result_status VARCHAR(30) NOT NULL DEFAULT 'Received',
  message VARCHAR(500) NULL,
  payload JSON NULL,
  UNIQUE KEY uq_webhook_provider_event (provider, event_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Khong tu dong chuyen don cu thanh da thanh toan.
-- Don cu duoc gan 'Chưa đối soát' de tranh ghi nhan nham doanh thu.
