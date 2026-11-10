package ttviet.view;

import ttviet.dao.DB;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Màn hình bếp: nhận món từ đơn hàng và cập nhật tiến độ chế biến. */
public class KitchenPanel extends JPanel {
    private final JTable grid = new JTable(new DefaultTableModel(
        new Object[]{"Mã món","Mã đơn","Bàn","Món","SL","Ghi chú","Trạng thái"},0) {
        @Override public boolean isCellEditable(int r,int c){return false;}
    });
    public KitchenPanel() {
        setLayout(new BorderLayout(10,10)); setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JLabel title=new JLabel("BẾP • DANH SÁCH MÓN CẦN CHẾ BIẾN");
        title.setFont(new Font("SansSerif",Font.BOLD,20)); title.setForeground(new Color(30,112,166));
        JButton refresh=new JButton("Làm mới");
        JButton cooking=new JButton("Đang chế biến");
        JButton done=new JButton("Hoàn thành");
        JButton unavailable=new JButton("Báo hết món");
        JPanel top=new JPanel(new BorderLayout()); top.setOpaque(false); top.add(title,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        actions.add(refresh); actions.add(cooking); actions.add(done); actions.add(unavailable);
        add(top,BorderLayout.NORTH); add(new JScrollPane(grid),BorderLayout.CENTER); add(actions,BorderLayout.SOUTH);
        refresh.addActionListener(e->load());
        cooking.addActionListener(e->update("Đang chế biến"));
        done.addActionListener(e->update("Hoàn thành"));
        unavailable.addActionListener(e->update("Hết món"));
        grid.setRowHeight(28); load();
    }
    private void load() {
        DefaultTableModel m=(DefaultTableModel)grid.getModel(); m.setRowCount(0);
        String sql="SELECT d.id,o.order_code,COALESCE(t.table_name,'Mang đi'),p.name,d.quantity,d.note,d.kitchen_status "+
            "FROM order_details d JOIN orders o ON o.id=d.order_id JOIN products p ON p.id=d.product_id "+
            "LEFT JOIN cafe_tables t ON t.id=o.table_id "+
            "WHERE COALESCE(o.payment_status,'')<>'Đã thanh toán' ORDER BY o.created_at,d.id";
        try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){
            while(r.next())m.addRow(new Object[]{r.getLong(1),r.getString(2),r.getString(3),r.getString(4),r.getInt(5),r.getString(6),r.getString(7)});
        } catch(Exception ex){error(ex);}
    }
    private void update(String status) {
        int row=grid.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Chọn món cần cập nhật.");return;}
        long id=((Number)grid.getValueAt(row,0)).longValue();
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("UPDATE order_details SET kitchen_status=? WHERE id=?")){
            p.setString(1,status);p.setLong(2,id);p.executeUpdate();load();
        }catch(Exception ex){error(ex);}
    }
    private void error(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi bếp",JOptionPane.ERROR_MESSAGE);}
}
