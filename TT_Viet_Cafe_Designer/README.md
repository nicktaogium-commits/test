# T & T Viet Cafe - NetBeans 30
Mở thư mục này bằng File > Open Project trong Apache NetBeans IDE 30 (Maven). Java 17+, XAMPP MySQL.

1. Chạy database/tt_viet.sql bằng phpMyAdmin.
2. Sửa user/password MySQL trong src/main/java/ttviet/dao/DB.java nếu cần.
3. Run project, tài khoản mặc định admin / 123456.
4. Mở các JFrame trong src/main/java/ttviet/view và chọn tab Design. Mỗi JFrame có cặp .java + .form đi cùng tên để chỉnh bằng GUI Builder.

Có LoginFrame, MainFrame, MenuFrame, OrderFrame, HistoryFrame. Giao diện tông đen - đồng vàng. Danh mục SQL chứa 37 món theo menu đã cung cấp. Ảnh món thật chưa được thêm vì chưa có bộ ảnh gốc; thư mục assets/food để bổ sung sau.

Lưu ý: đây là bản nền học tập; thanh toán hiện mới tính tổng và chưa tích hợp máy in/QR. Phần lưu hóa đơn chi tiết cần hoàn thiện thêm trước khi dùng kinh doanh thực tế.