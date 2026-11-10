package ttviet;
import java.sql.*;
public final class DB {
 private static final String DB_URL = "jdbc:mysql://localhost:3306/tt_viet_cafe?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh";
 private static final String DB_USER = "root";
 private static final String DB_PASSWORD = "";
 private DB() {}
 public static Connection getConnection() throws SQLException {
  return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
 }
}