package ttviet.dao;

import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class OrderDAO {
    public static boolean save(DefaultTableModel model) {
        return saveAndGetId(model) > 0;
    }

    /** Lưu đơn và trả về ID để mở/in đúng hóa đơn vừa tạo; -1 nếu thất bại. */
    public static long saveAndGetId(DefaultTableModel model) {
        String code = "TT" + System.currentTimeMillis();
        String totalSql = "INSERT INTO orders(order_code,user_id,total,status) " +
            "VALUES(?,(SELECT id FROM users WHERE username='admin' LIMIT 1),?,'Đã thanh toán')";
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                double total = 0;
                for (int i = 0; i < model.getRowCount(); i++)
                    total += Double.parseDouble(model.getValueAt(i, 3).toString());
                long id;
                try (PreparedStatement p = c.prepareStatement(totalSql, Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1, code);
                    p.setDouble(2, total);
                    p.executeUpdate();
                    try (ResultSet r = p.getGeneratedKeys()) {
                        if (!r.next()) throw new SQLException("Không lấy được mã hóa đơn vừa tạo.");
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
                        p.setDouble(4, Double.parseDouble(model.getValueAt(i, 1).toString()));
                        p.setDouble(5, Double.parseDouble(model.getValueAt(i, 3).toString()));
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
