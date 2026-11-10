package ttviet.Dao;
import java.sql.*;
public class DBConnection {
 private static final String URL="jdbc:mysql://localhost:3306/tt_viet_cafe?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh";
 private static final String USER="root",PASSWORD="";
 private DBConnection(){}
 public static Connection getConnection() throws SQLException{return DriverManager.getConnection(URL,USER,PASSWORD);}
}