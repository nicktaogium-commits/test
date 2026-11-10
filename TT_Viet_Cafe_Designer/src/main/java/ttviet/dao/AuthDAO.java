package ttviet.dao;

import java.sql.*;

public class AuthDAO {
    public static boolean login(String username, String password) {
        String sql = "SELECT id,username,full_name,role FROM users WHERE username=? AND password=?";
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, password);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return false;
                Session.setUser(r.getInt("id"), r.getString("username"),
                    r.getString("full_name"), r.getString("role"));
                return true;
            }
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Lỗi MySQL: " + e.getMessage());
            return false;
        }
    }
}
