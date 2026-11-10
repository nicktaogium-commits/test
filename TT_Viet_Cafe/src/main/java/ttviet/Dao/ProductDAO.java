package ttviet.Dao;
import ttviet.Model.Product;import java.sql.*;import java.util.*;
public class ProductDAO {
 public List<Product> getAll()throws SQLException{
  List<Product> a=new ArrayList<>();
  try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,name,category,price FROM products WHERE active=1 ORDER BY category,name")){
   while(r.next())a.add(new Product(r.getInt(1),r.getString(2),r.getString(3),r.getBigDecimal(4)));
  }return a;
 }
}