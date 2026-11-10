package ttviet.dao;
import java.sql.*;import javax.swing.table.DefaultTableModel;
public class HistoryDAO {
 public static DefaultTableModel load(){DefaultTableModel m=new DefaultTableModel(new String[]{"Mã đơn","Thời gian","Tổng tiền","Trạng thái"},0);String q="SELECT order_code,created_at,total,status FROM orders ORDER BY id DESC";
 try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){while(r.next())m.addRow(new Object[]{r.getString(1),r.getTimestamp(2),r.getBigDecimal(3),r.getString(4)});}catch(SQLException e){javax.swing.JOptionPane.showMessageDialog(null,e.getMessage());}return m;}
}