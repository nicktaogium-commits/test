# T & T VIET — Quản lý nhà hàng / quán cà phê

Java Swing + Maven + MySQL, dùng Apache NetBeans IDE 25 (JDK 17+).

## Cài đặt lần đầu
1. Mở project `TT_Viet_Cafe_Designer` bằng File → Open Project trong NetBeans 25.
2. Bật MySQL trong XAMPP.
3. Import file `database/tt_viet.sql` bằng phpMyAdmin.
4. Import tiếp `database/upgrade_nghiep_vu.sql` để tạo bảng nhân viên, phân ca, bồi thường tài sản và thêm bàn/khu vực.
5. Import tiếp `database/upgrade_vai_tro_bep_kho.sql` để bổ sung tài khoản theo vai trò, màn hình bếp và quản lý kho. File này được viết để chạy lại mà không xóa dữ liệu cũ.
6. Mở `src/main/java/ttviet/dao/DB.java` để sửa user/password MySQL nếu khác cấu hình mặc định.
7. Chạy Main Class `ttviet.App` (F6). Tài khoản demo (mật khẩu đều là `123456`):
   - Quản lý: `admin`
   - Thu ngân: `cashier`
   - Phục vụ: `service`
   - Bếp: `kitchen`
   - Kho: `warehouse`

## Màn hình
- `LoginFrame`: đăng nhập.
- `OperationsFrame`: trung tâm nghiệp vụ, giao diện trắng - xanh và tự lọc tab theo vai trò.
- `AccountManagementPanel`: quản lý tạo tài khoản, gán vai trò và đặt lại mật khẩu.
- `KitchenPanel`: danh sách món chưa thanh toán, cập nhật chờ chế biến / đang chế biến / hoàn thành / hết món.
- `WarehousePanel`: danh mục nguyên liệu, nhập kho, xuất kho và số lượng tồn.
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
- Quản lý nhân viên; thêm/sửa/xóa và liên kết tài khoản đăng nhập.
- 5 vai trò: Quản lý, Thu ngân, Phục vụ, Bếp, Kho; các màn hình được lọc theo vai trò.
- Màn hình bếp nhận món từ đơn hàng và cập nhật tiến độ chế biến.
- Quản lý nguyên liệu, số lượng tồn, mức cảnh báo, phiếu nhập và phiếu xuất.
- Phân ca nhân viên theo ngày, giờ và loại ca.
- Ghi nhận bồi thường tài sản, số lượng, đơn giá, tổng tiền.
- Lịch sử hóa đơn và xem chi tiết.
- Báo cáo doanh thu theo khoảng ngày, số hóa đơn, bồi thường và top món bán chạy.
- Chuyển đơn đang mở giữa bàn; mục gộp/tách hiện mới là thao tác giao diện, cần hoàn thiện quy tắc nghiệp vụ và kiểm thử thêm.

## Lưu ý tình trạng
Đây là bản phát triển học tập, chưa được chạy kiểm thử end-to-end trên máy của bạn. Trước khi nộp, hãy import cả ba file SQL theo đúng thứ tự và kiểm thử từng luồng. Cần kiểm thử phân quyền và các luồng nghiệp vụ trên máy thực tế. Kho hiện quản lý nhập/xuất/tồn độc lập; chưa tự động trừ nguyên liệu theo công thức định lượng món. Mật khẩu demo đang dùng cách lưu đơn giản tương thích với AuthDAO hiện tại, chỉ dùng để thử nghiệm học tập và cần thay đổi trước khi triển khai thật. Ảnh món ăn thật chưa có trong nguồn nên chưa gắn ảnh sản phẩm.
