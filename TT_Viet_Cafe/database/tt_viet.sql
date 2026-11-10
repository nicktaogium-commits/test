CREATE DATABASE IF NOT EXISTS tt_viet_cafe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tt_viet_cafe;
CREATE TABLE IF NOT EXISTS users (
 id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE,
 password VARCHAR(100) NOT NULL, full_name VARCHAR(100) NOT NULL, role VARCHAR(30) NOT NULL DEFAULT 'cashier'
);
CREATE TABLE IF NOT EXISTS products (
 id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(150) NOT NULL, category VARCHAR(80) NOT NULL,
 price DECIMAL(12,2) NOT NULL, active TINYINT(1) NOT NULL DEFAULT 1
);
CREATE TABLE IF NOT EXISTS cafe_tables (
 id INT AUTO_INCREMENT PRIMARY KEY, table_name VARCHAR(30) NOT NULL UNIQUE,
 status ENUM('TRỐNG','ĐANG DÙNG') NOT NULL DEFAULT 'TRỐNG'
);
CREATE TABLE IF NOT EXISTS orders (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, order_code VARCHAR(30) NOT NULL UNIQUE,
 table_id INT NULL, user_id INT NULL, total DECIMAL(12,2) NOT NULL DEFAULT 0,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_order_table FOREIGN KEY(table_id) REFERENCES cafe_tables(id),
 CONSTRAINT fk_order_user FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS order_items (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, product_id INT NOT NULL,
 quantity INT NOT NULL, unit_price DECIMAL(12,2) NOT NULL,
 CONSTRAINT fk_item_order FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE,
 CONSTRAINT fk_item_product FOREIGN KEY(product_id) REFERENCES products(id)
);
INSERT IGNORE INTO users(username,password,full_name,role) VALUES('admin','123456','Quản trị viên','admin');
INSERT IGNORE INTO cafe_tables(table_name) VALUES ('Bàn 01'),('Bàn 02'),('Bàn 03'),('Bàn 04'),('Bàn 05'),('Bàn 06'),('Mang đi');
INSERT INTO products(name,category,price) SELECT 'Pizza Mini Xúc Xích Phô Mai','Thức ăn nhanh',19000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Pizza Mini Xúc Xích Phô Mai');
INSERT INTO products(name,category,price) SELECT 'Pizza Mini Bò Phô Mai','Thức ăn nhanh',19000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Pizza Mini Bò Phô Mai');
INSERT INTO products(name,category,price) SELECT 'Bánh Mì Que Pate Chà Bông','Thức ăn nhanh',15000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Bánh Mì Que Pate Chà Bông');
INSERT INTO products(name,category,price) SELECT 'Bánh Mì Que Bò Phô Mai','Thức ăn nhanh',19000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Bánh Mì Que Bò Phô Mai');
INSERT INTO products(name,category,price) SELECT 'Cơm Nắm Bò Phô Mai','Thức ăn nhanh',17000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Cơm Nắm Bò Phô Mai');
INSERT INTO products(name,category,price) SELECT 'Cơm Nắm Xúc Xích Phô Mai','Thức ăn nhanh',17000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Cơm Nắm Xúc Xích Phô Mai');
INSERT INTO products(name,category,price) SELECT 'Cơm Nắm Cá Hồi Mayo','Thức ăn nhanh',17000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Cơm Nắm Cá Hồi Mayo');
INSERT INTO products(name,category,price) SELECT 'Rau Má','Thức uống tự nhiên',20000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Rau Má');
INSERT INTO products(name,category,price) SELECT 'Rau Má Sữa','Thức uống tự nhiên',22000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Rau Má Sữa');
INSERT INTO products(name,category,price) SELECT 'Rau Má Dừa','Thức uống tự nhiên',22000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Rau Má Dừa');
INSERT INTO products(name,category,price) SELECT 'Dừa','Thức uống tự nhiên',22000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Dừa');
INSERT INTO products(name,category,price) SELECT 'Dừa Tắc Thái Lan','Thức uống tự nhiên',25000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Dừa Tắc Thái Lan');
INSERT INTO products(name,category,price) SELECT 'Đá Me (Best Seller)','Thức uống tự nhiên',22000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Đá Me (Best Seller)');
INSERT INTO products(name,category,price) SELECT 'Đá Chanh','Thức uống tự nhiên',18000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Đá Chanh');
INSERT INTO products(name,category,price) SELECT 'Chanh Muối','Thức uống tự nhiên',18000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Chanh Muối');
INSERT INTO products(name,category,price) SELECT 'Tắc Xí Muội','Thức uống tự nhiên',18000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Tắc Xí Muội');
INSERT INTO products(name,category,price) SELECT 'Cam Vắt','Thức uống tự nhiên',22000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Cam Vắt');
INSERT INTO products(name,category,price) SELECT 'Cam Sữa','Thức uống tự nhiên',27000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Cam Sữa');
INSERT INTO products(name,category,price) SELECT 'Chanh Dây','Thức uống tự nhiên',22000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Chanh Dây');
INSERT INTO products(name,category,price) SELECT 'Bò Cụng','Nước ngọt',22000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Bò Cụng');
INSERT INTO products(name,category,price) SELECT 'Sting','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Sting');
INSERT INTO products(name,category,price) SELECT 'Sting Thái','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Sting Thái');
INSERT INTO products(name,category,price) SELECT 'Dr Thanh','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Dr Thanh');
INSERT INTO products(name,category,price) SELECT 'Pessi','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Pessi');
INSERT INTO products(name,category,price) SELECT 'Ô Lông','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Ô Lông');
INSERT INTO products(name,category,price) SELECT 'Không Độ','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Không Độ');
INSERT INTO products(name,category,price) SELECT 'CoCa','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='CoCa');
INSERT INTO products(name,category,price) SELECT 'Number 1','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Number 1');
INSERT INTO products(name,category,price) SELECT '7 Up','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='7 Up');
INSERT INTO products(name,category,price) SELECT 'Đậu Nành','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Đậu Nành');
INSERT INTO products(name,category,price) SELECT '247','Nước ngọt',16000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='247');
INSERT INTO products(name,category,price) SELECT 'Suối','Nước ngọt',10000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Suối');
INSERT INTO products(name,category,price) SELECT 'Trà Sữa Truyền Thống','Trà sữa đóng chai',15000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Trà Sữa Truyền Thống');
INSERT INTO products(name,category,price) SELECT 'Trà Sữa Thái Xanh','Trà sữa đóng chai',15000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Trà Sữa Thái Xanh');
INSERT INTO products(name,category,price) SELECT 'Trà Sữa Thái Đỏ','Trà sữa đóng chai',15000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Trà Sữa Thái Đỏ');
INSERT INTO products(name,category,price) SELECT 'Cafe - Bánh Mì Que Pate Chà Bông','Combo An Nhiên',29000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Cafe - Bánh Mì Que Pate Chà Bông');
INSERT INTO products(name,category,price) SELECT 'Cafe Sữa - Bánh Mì Que Pate Chà Bông','Combo An Nhiên',34000 WHERE NOT EXISTS(SELECT 1 FROM products WHERE name='Cafe Sữa - Bánh Mì Que Pate Chà Bông');