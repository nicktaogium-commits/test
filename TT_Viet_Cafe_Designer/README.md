# T & T VIET — Quản lý nhà hàng / quán cà phê

Dự án Java Swing Maven, cấu hình chạy với Apache NetBeans IDE 25 và JDK 17+.

## Mở và chạy
1. NetBeans 25 → File → Open Project → chọn thư mục `TT_Viet_Cafe_Designer`.
2. Chờ Maven tải MySQL Connector/J.
3. Mở XAMPP, bật MySQL; vào phpMyAdmin và Import `database/tt_viet.sql`.
4. Kiểm tra `src/main/java/ttviet/dao/DB.java`: mặc định host localhost, database tt_viet_cafe, user root, mật khẩu rỗng.
5. Main class: `ttviet.App`. Nếu NetBeans hỏi, chọn package `ttviet` → `App.java`.
6. Chạy F6. Tài khoản mẫu: admin / 123456.

## Các JFrame hiện có
- Đăng nhập: `LoginFrame`
- Trang tổng quan: `MainFrame`
- Thực đơn: `MenuFrame`
- Gọi món / thanh toán: `OrderFrame`
- Lịch sử hóa đơn: `HistoryFrame`
- Sơ đồ bàn: `TableFrame`

Mỗi JFrame hiện có cặp file .java + .form. Mở file trong package `ttviet.view`, chọn tab Design để chỉnh sửa bằng GUI Builder.

## Phạm vi đề tài cần hoàn thiện
Đăng nhập/đăng xuất theo vai trò và ca làm; sơ đồ bàn/khu vực theo trạng thái; nhân viên và phân ca; thực đơn, nhóm món, giá và trạng thái còn/hết; order, ghi chú, chuyển/gộp/tách bàn; thanh toán và hóa đơn; ghi nhận bồi thường tài sản; báo cáo doanh thu và món bán chạy.

## Tình trạng
Đây là bản nền đang được mở rộng theo đề tài, chưa phải phần mềm đã nghiệm thu hoặc sẵn sàng dùng kinh doanh. Cần chạy thử từng luồng nghiệp vụ, kiểm tra dữ liệu MySQL và hoàn thiện các màn hình còn thiếu. Ảnh món ăn thật chưa được cung cấp nên chưa thể gắn ảnh thực tế cho từng món.
