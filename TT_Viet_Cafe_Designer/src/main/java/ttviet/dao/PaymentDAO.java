package ttviet.dao;

import java.math.BigDecimal;
import java.sql.*;

/** Nghiep vu thanh toan: chi danh dau da thu tien sau khi thu ngan xac nhan tien mat. */
public final class PaymentDAO {
    private PaymentDAO() {}

    private static void requireCashierOrManager() throws SQLException {
        String role=Session.getRole()==null ? "" : Session.getRole().trim().toLowerCase(java.util.Locale.ROOT);
        boolean allowed=role.equals("manager") || role.equals("admin") || role.equals("quản lý") ||
            role.equals("quan ly") || role.equals("cashier") || role.equals("thu ngân") || role.equals("thu ngan");
        if(!allowed)throw new SQLException("Chỉ thu ngân hoặc quản lý mới có quyền xác nhận thanh toán.");
    }

    public static boolean payCash(long orderId, BigDecimal received) throws SQLException {
        requireCashierOrManager();
        if (received == null || received.compareTo(BigDecimal.ZERO) <= 0)
            throw new SQLException("So tien khach dua khong hop le.");
        try (Connection c=DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                BigDecimal total;
                int tableId;
                try (PreparedStatement q=c.prepareStatement(
                    "SELECT total,table_id,payment_status FROM orders WHERE id=? FOR UPDATE")) {
                    q.setLong(1,orderId);
                    try(ResultSet r=q.executeQuery()) {
                        if(!r.next()) throw new SQLException("Khong tim thay hoa don.");
                        if("Đã thanh toán".equals(r.getString(3))) throw new SQLException("Hoa don nay da thanh toan.");
                        total=r.getBigDecimal(1);
                        tableId=r.getInt(2);
                        if(r.wasNull()) tableId=0;
                    }
                }
                if(received.compareTo(total)<0) throw new SQLException("So tien khach dua chua du.");
                try(PreparedStatement p=c.prepareStatement(
                    "UPDATE orders SET payment_status='Đã thanh toán',payment_method='TIEN_MAT',paid_at=NOW(),status='Đã thanh toán' WHERE id=?")) {
                    p.setLong(1,orderId);
                    if(p.executeUpdate()!=1) throw new SQLException("Khong cap nhat duoc hoa don.");
                }
                releaseTableIfAllPaid(c,tableId);
                c.commit();
                return true;
            } catch(Exception ex) {
                c.rollback();
                if(ex instanceof SQLException) throw (SQLException)ex;
                throw new SQLException(ex);
            }
        }
    }

    public static boolean isPaid(long orderId) throws SQLException {
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(
            "SELECT payment_status FROM orders WHERE id=?")) {
            p.setLong(1,orderId);
            try(ResultSet r=p.executeQuery()) { return r.next() && "Đã thanh toán".equals(r.getString(1)); }
        }
    }

    public static BigDecimal getTotal(long orderId) throws SQLException {
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(
            "SELECT total FROM orders WHERE id=?")) {
            p.setLong(1,orderId);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next()) throw new SQLException("Khong tim thay hoa don.");
                return r.getBigDecimal(1);
            }
        }
    }

    private static void releaseTableIfAllPaid(Connection c,int tableId) throws SQLException {
        if(tableId<=0)return;
        try(PreparedStatement p=c.prepareStatement(
            "UPDATE cafe_tables SET status='Trống' WHERE id=? AND NOT EXISTS("+
            "SELECT 1 FROM orders o WHERE o.table_id=? AND o.status='Đang phục vụ' "+
            "AND (o.payment_status IS NULL OR o.payment_status<>'Đã thanh toán'))")) {
            p.setInt(1,tableId);p.setInt(2,tableId);p.executeUpdate();
        }
    }
}
