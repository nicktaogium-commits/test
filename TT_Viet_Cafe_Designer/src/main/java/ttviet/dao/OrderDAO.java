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
}
