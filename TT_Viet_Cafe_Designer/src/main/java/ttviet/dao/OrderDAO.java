package ttviet.dao;

import java.math.BigDecimal;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/** Luu don o trang thai chua thanh toan; viec in phieu khong dong nghia da thu tien. */
public class OrderDAO {
    public static boolean save(DefaultTableModel model) {
        return saveAndGetId(model, null) > 0;
    }

    public static long saveAndGetId(DefaultTableModel model) {
        return saveAndGetId(model, null);
    }

    public static long saveAndGetId(DefaultTableModel model, Integer tableId) {
        if (model == null || model.getRowCount() == 0) return -1;
        String code = "TTV" + System.currentTimeMillis();
        String sql = "INSERT INTO orders(order_code,table_id,user_id,total,status,payment_status,payment_method) "
                   + "VALUES(?,?,?,?,'Đang phục vụ','Chờ thanh toán',NULL)";
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                BigDecimal total = BigDecimal.ZERO;
                for (int i = 0; i < model.getRowCount(); i++) {
                    Object line = model.getValueAt(i, 3);
                    total = total.add(new BigDecimal(line.toString()));
                }
                if (Session.getUserId() <= 0)
                    throw new SQLException("Phiên đăng nhập đã hết. Vui lòng đăng nhập lại.");
                long id;
                try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1, code);
                    if (tableId == null) p.setNull(2, Types.INTEGER); else p.setInt(2, tableId);
                    p.setInt(3, Session.getUserId());
                    p.setBigDecimal(4, total);
                    p.executeUpdate();
                    try (ResultSet r = p.getGeneratedKeys()) {
                        if (!r.next()) throw new SQLException("Không lấy được mã đơn vừa tạo.");
                        id = r.getLong(1);
                    }
                }
                String find = "SELECT id FROM products WHERE name=?";
                String detail = "INSERT INTO order_details(order_id,product_id,quantity,unit_price,line_total) VALUES(?,?,?,?,?)";
                for (int i = 0; i < model.getRowCount(); i++) {
                    int productId;
                    try (PreparedStatement p = c.prepareStatement(find)) {
                        p.setString(1, model.getValueAt(i, 0).toString());
                        try (ResultSet r = p.executeQuery()) {
                            if (!r.next()) throw new SQLException("Không tìm thấy món " + model.getValueAt(i, 0));
                            productId = r.getInt(1);
                        }
                    }
                    try (PreparedStatement p = c.prepareStatement(detail)) {
                        p.setLong(1, id);
                        p.setInt(2, productId);
                        p.setInt(3, Integer.parseInt(model.getValueAt(i, 2).toString()));
                        p.setBigDecimal(4, new BigDecimal(model.getValueAt(i, 1).toString()));
                        p.setBigDecimal(5, new BigDecimal(model.getValueAt(i, 3).toString()));
                        p.executeUpdate();
                    }
                }
                if (tableId != null) {
                    try (PreparedStatement p = c.prepareStatement(
                            "UPDATE cafe_tables SET status='Đang phục vụ' WHERE id=?")) {
                        p.setInt(1, tableId);
                        p.executeUpdate();
                    }
                }
                c.commit();
                return id;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Không lưu được đơn: " + e.getMessage());
            return -1;
        }
    }

    /** Them mon vao don chua thanh toan cua ban; neu chua co don thi tao moi. */
    public static long addItemsToOpenTable(DefaultTableModel model, int tableId) {
        if (model == null || model.getRowCount() == 0 || tableId <= 0) return -1;
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (Session.getUserId() <= 0) throw new SQLException("Phiên đăng nhập đã hết.");
                long orderId = -1;
                try (PreparedStatement q = c.prepareStatement(
                    "SELECT id FROM orders WHERE table_id=? AND status='Đang phục vụ' " +
                    "AND (payment_status IS NULL OR payment_status<>'Đã thanh toán') ORDER BY id DESC LIMIT 1 FOR UPDATE")) {
                    q.setInt(1, tableId);
                    try (ResultSet rs = q.executeQuery()) { if (rs.next()) orderId = rs.getLong(1); }
                }
                if (orderId < 0) {
                    String code = "TTV" + System.currentTimeMillis();
                    try (PreparedStatement q = c.prepareStatement(
                        "INSERT INTO orders(order_code,table_id,user_id,total,status,payment_status,payment_method) " +
                        "VALUES(?,?,?,0,'Đang phục vụ','Chờ thanh toán',NULL)", Statement.RETURN_GENERATED_KEYS)) {
                        q.setString(1, code); q.setInt(2, tableId); q.setInt(3, Session.getUserId());
                        q.executeUpdate();
                        try (ResultSet rs = q.getGeneratedKeys()) {
                            if (!rs.next()) throw new SQLException("Không tạo được đơn cho bàn.");
                            orderId = rs.getLong(1);
                        }
                    }
                }
                for (int i=0; i<model.getRowCount(); i++) {
                    String name = model.getValueAt(i,0).toString();
                    int qty = Integer.parseInt(model.getValueAt(i,2).toString());
                    BigDecimal price = new BigDecimal(model.getValueAt(i,1).toString());
                    int productId;
                    try (PreparedStatement q = c.prepareStatement("SELECT id FROM products WHERE name=?")) {
                        q.setString(1,name);
                        try (ResultSet rs=q.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Không tìm thấy món: "+name);
                            productId=rs.getInt(1);
                        }
                    }
                    try (PreparedStatement q = c.prepareStatement(
                        "SELECT id,quantity FROM order_details WHERE order_id=? AND product_id=? LIMIT 1 FOR UPDATE")) {
                        q.setLong(1,orderId); q.setInt(2,productId);
                        try (ResultSet rs=q.executeQuery()) {
                            if (rs.next()) {
                                int detailId=rs.getInt(1), newQty=rs.getInt(2)+qty;
                                try (PreparedStatement u=c.prepareStatement(
                                    "UPDATE order_details SET quantity=?,unit_price=?,line_total=? WHERE id=?")) {
                                    u.setInt(1,newQty); u.setBigDecimal(2,price);
                                    u.setBigDecimal(3,price.multiply(BigDecimal.valueOf(newQty))); u.setInt(4,detailId); u.executeUpdate();
                                }
                            } else {
                                try (PreparedStatement u=c.prepareStatement(
                                    "INSERT INTO order_details(order_id,product_id,quantity,unit_price,line_total) VALUES(?,?,?,?,?)")) {
                                    u.setLong(1,orderId); u.setInt(2,productId); u.setInt(3,qty); u.setBigDecimal(4,price);
                                    u.setBigDecimal(5,price.multiply(BigDecimal.valueOf(qty))); u.executeUpdate();
                                }
                            }
                        }
                    }
                }
                try (PreparedStatement q=c.prepareStatement(
                    "UPDATE orders SET total=(SELECT COALESCE(SUM(line_total),0) FROM order_details WHERE order_id=?) WHERE id=?")) {
                    q.setLong(1,orderId); q.setLong(2,orderId); q.executeUpdate();
                }
                try (PreparedStatement q=c.prepareStatement("UPDATE cafe_tables SET status='Đang phục vụ' WHERE id=?")) {
                    q.setInt(1,tableId); q.executeUpdate();
                }
                c.commit(); return orderId;
            } catch (Exception ex) { c.rollback(); throw ex; }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Không thêm được món vào bàn: "+ex.getMessage());
            return -1;
        }
    }

}
