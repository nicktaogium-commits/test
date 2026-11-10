package ttviet.dao;
import java.sql.*;import javax.swing.table.DefaultTableModel;
public class OrderDAO {
 public static boolean save(DefaultTableModel m){String code="TT"+System.currentTimeMillis();String totalSql="INSERT INTO orders(order_code,user_id,total,status) VALUES(?,(SELECT id FROM users WHERE username='admin' LIMIT 1),?,'Đã thanh toán')";
 try(Connection c=DB.getConnection()){c.setAutoCommit(false);double total=0;for(int i=0;i<m.getRowCount();i++)total+=Double.parseDouble(m.getValueAt(i,3).toString());
  long id;try(PreparedStatement p=c.prepareStatement(totalSql,Statement.RETURN_GENERATED_KEYS)){p.setString(1,code);p.setDouble(2,total);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();id=r.getLong(1);}}
  String find="SELECT id FROM products WHERE name=?";String detail="INSERT INTO order_details(order_id,product_id,quantity,unit_price,line_total) VALUES(?,?,?,?,?)";
  for(int i=0;i<m.getRowCount();i++){int pid;try(PreparedStatement p=c.prepareStatement(find)){p.setString(1,m.getValueAt(i,0).toString());try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Không tìm thấy món "+m.getValueAt(i,0));pid=r.getInt(1);}}
   try(PreparedStatement p=c.prepareStatement(detail)){p.setLong(1,id);p.setInt(2,pid);p.setInt(3,Integer.parseInt(m.getValueAt(i,2).toString()));p.setDouble(4,Double.parseDouble(m.getValueAt(i,1).toString()));p.setDouble(5,Double.parseDouble(m.getValueAt(i,3).toString()));p.executeUpdate();}}
  c.commit();return true;
 }catch(Exception e){javax.swing.JOptionPane.showMessageDialog(null,"Không lưu được đơn: "+e.getMessage());return false;}}
}