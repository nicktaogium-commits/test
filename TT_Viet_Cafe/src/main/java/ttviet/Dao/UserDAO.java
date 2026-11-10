package ttviet.Dao;
import ttviet.Model.User;import java.sql.*;
public class UserDAO {
 public User login(String username,String password)throws SQLException{
  String sql="SELECT id,username,full_name,role FROM users WHERE username=? AND password=?";
  try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
   p.setString(1,username);p.setString(2,password);
   try(ResultSet r=p.executeQuery()){if(r.next())return new User(r.getInt(1),r.getString(2),r.getString(3),r.getString(4));}
  }return null;
 }
}