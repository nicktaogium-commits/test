# T & T VIET — Quản lý nhà hàng / quán cà phê

Java Swing + Maven + MySQL, dùng Apache NetBeans IDE 25 (JDK 17+).

## Cài đặt lần đầu
1. Mở project `TT_Viet_Cafe_Designer` bằng File → Open Project trong NetBeans 25.
2. Bật MySQL trong XAMPP.
3. Import file `database/tt_viet.sql` bằng phpMyAdmin.
4. Import tiếp `database/upgrade_nghiep_vu.sql` để tạo bảng nhân viên, phân ca, bồi thường tài sản và thêm bàn/khu vực.
5. Mở `src/main/java/ttviet/dao/DB.java` để sửa user/password MySQL nếu khác cấu hình mặc định.
6. Chạy Main Class `ttviet.App` (F6). Tài khoản demo: `admin` / `123456`.

## Màn hình
- `LoginFrame`: đăng nhập.
- `OperationsFrame`: trung tâm nghiệp vụ, giao diện tối đen than + vàng đồng, các thẻ tổng quan và tab nghiệp vụ.
- `MenuFrame`: thực đơn hiện có.
- `OrderFrame`: giỏ món và thanh toán hiện có.
- `TableFrame`: màn hình bàn hiện có.
- `HistoryFrame`: lịch sử hóa đơn hiện có.

Các JFrame cũ có cặp `.java` + `.form` để mở Design. `OperationsFrame` là cửa sổ điều hành bổ sung đang được dựng bằng Swing code; chưa có file `.form` GUI Builder riêng.

## Nghiệp vụ đã có trong bản hiện tại
- Đăng nhập bằng tài khoản mẫu.
- Danh sách 37 món, nhóm món, giá và trạng thái còn/hết.
- Thêm/sửa/xóa món.
- Gọi món, giỏ hàng và lưu hóa đơn / thanh toán.
- Sơ đồ danh sách bàn, đổi trạng thái trống/đang phục vụ/đặt trước.
- Quản lý nhân viên; thêm/sửa/xóa.
- Phân ca nhân viên theo ngày, giờ và loại ca.
- Ghi nhận bồi thường tài sản, số lượng, đơn giá, tổng tiền.
- Lịch sử hóa đơn và xem chi tiết.
- Báo cáo doanh thu theo khoảng ngày, số hóa đơn, bồi thường và top món bán chạy.
- Chuyển đơn đang mở giữa bàn; mục gộp/tách hiện mới là thao tác giao diện, cần hoàn thiện quy tắc nghiệp vụ và kiểm thử thêm.

## Lưu ý tình trạng
Đây là bản phát triển học tập, chưa được chạy kiểm thử end-to-end trên máy của bạn. Trước khi nộp, hãy import cả hai file SQL theo đúng thứ tự và kiểm thử từng luồng. Cần hoàn thiện thêm phân quyền thao tác theo vai trò/ca, in hóa đơn, chuyển/gộp/tách bàn đầy đủ, và kiểm tra ràng buộc dữ liệu. Ảnh món ăn thật chưa có trong nguồn nên chưa gắn ảnh sản phẩm.
