# T & T Viet – Quản lý quán cà phê / thức ăn nhanh

Dự án Java Swing cho **Apache NetBeans IDE 30**, Java 17+, Maven và MySQL (XAMPP).

## Mở dự án
1. Tải thư mục `TT_Viet_Cafe` về máy (Code → Download ZIP hoặc tải cả repository).
2. NetBeans: **File → Open Project** → chọn thư mục `TT_Viet_Cafe`.
3. Mở XAMPP, bật MySQL. Vào phpMyAdmin → Import → chọn `database/tt_viet.sql`.
4. Mở `src/main/java/ttviet/DB.java`, chỉnh DB_URL/DB_USER/DB_PASSWORD nếu MySQL của bạn khác mặc định.
5. Nhấn **Clean and Build**, sau đó Run.

Tài khoản demo: **admin / 123456**. Hãy đổi mật khẩu sau khi thử nghiệm.

## Chức năng bản khởi tạo
- Đăng nhập; giao diện dashboard tông đen – vàng đồng.
- Danh mục 37 món ăn/uống được nạp từ MySQL.
- Tạo hóa đơn nhanh, tính tổng, lưu hóa đơn và chi tiết đơn hàng.
- Xem lịch sử hóa đơn.
- Quản lý trạng thái bàn cơ bản.
- Các màn hình LoginFrame và MainFrame có cặp tệp `.java` + `.form` để mở trong NetBeans GUI Builder.

## Ảnh món ăn
Thư mục `assets/food` được để sẵn cho ảnh do quán cung cấp. Bản này chưa gắn ảnh sản phẩm thật vì bạn chưa gửi bộ ảnh; không dùng ảnh giả để đánh tráo ảnh thật.