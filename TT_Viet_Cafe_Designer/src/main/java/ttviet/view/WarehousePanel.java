package ttviet.view;

import ttviet.dao.DB;
import ttviet.dao.Session;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

/** Quản lý danh mục nguyên liệu và phiếu nhập/xuất kho cơ bản. */
public class WarehousePanel extends JPanel {
    private final JTable grid=new JTable(new DefaultTableModel(new Object[]{"Mã","Nguyên liệu","Đơn vị","Tồn kho","Mức cảnh báo"},0){
        @Override public boolean isCellEditable(int r,int c){return false;}
    });
    public WarehousePanel(){
        setLayout(new BorderLayout(10,10));setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JLabel title=new JLabel("KHO • NGUYÊN LIỆU & TỒN KHO");
        title.setFont(new Font("SansSerif",Font.BOLD,20));title.setForeground(new Color(30,112,166));
        JButton add=new JButton("Thêm nguyên liệu"),in=new JButton("Nhập kho"),out=new JButton("Xuất kho"),refresh=new JButton("Làm mới");
        JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);top.add(title,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));actions.setOpaque(false);
        actions.add(add);actions.add(in);actions.add(out);actions.add(refresh);
        add(top,BorderLayout.NORTH);add(new JScrollPane(grid),BorderLayout.CENTER);add(actions,BorderLayout.SOUTH);
        add.addActionListener(e->addItem());in.addActionListener(e->move(true));out.addActionListener(e->move(false));refresh.addActionListener(e->load());
        grid.setRowHeight(28);load();
    }
    private void load(){
        DefaultTableModel m=(DefaultTableModel)grid.getModel();m.setRowCount(0);
        try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(
            "SELECT id,item_name,unit_name,quantity,min_quantity FROM inventory_items WHERE active=1 ORDER BY item_name")){
            while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getBigDecimal(4),r.getBigDecimal(5)});
        }catch(Exception ex){error(ex);}
    }
    private void addItem(){
        JTextField name=new JTextField(),unit=new JTextField("kg"),min=new JTextField("0");
        JPanel f=new JPanel(new GridLayout(0,1,4,4));f.add(new JLabel("Tên nguyên liệu"));f.add(name);f.add(new JLabel("Đơn vị (kg, lít, cái...)"));f.add(unit);f.add(new JLabel("Mức cảnh báo tồn"));f.add(min);
        if(JOptionPane.showConfirmDialog(this,f,"Thêm nguyên liệu",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO inventory_items(item_name,unit_name,min_quantity) VALUES(?,?,?)")){
            p.setString(1,name.getText().trim());p.setString(2,unit.getText().trim());p.setBigDecimal(3,new BigDecimal(min.getText().trim()));p.executeUpdate();load();
        }catch(Exception ex){error(ex);}
    }
    private void move(boolean receive){
        int row=grid.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Chọn nguyên liệu trước.");return;}
        int id=((Number)grid.getValueAt(row,0)).intValue();
        JTextField qty=new JTextField("1"),note=new JTextField();
        JPanel f=new JPanel(new GridLayout(0,1,4,4));f.add(new JLabel("Số lượng"));f.add(qty);f.add(new JLabel("Ghi chú / lý do"));f.add(note);
        if(JOptionPane.showConfirmDialog(this,f,receive?"Phiếu nhập kho":"Phiếu xuất kho",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{
            BigDecimal amount=new BigDecimal(qty.getText().trim());
            if(amount.signum()<=0)throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
            try(Connection c=DB.getConnection()){
                c.setAutoCommit(false);
                try{
                    String update=receive?
                        "UPDATE inventory_items SET quantity=quantity+? WHERE id=?":
                        "UPDATE inventory_items SET quantity=quantity-? WHERE id=? AND quantity>=?";
                    try(PreparedStatement p=c.prepareStatement(update)){
                        p.setBigDecimal(1,amount);p.setInt(2,id);if(!receive)p.setBigDecimal(3,amount);
                        if(p.executeUpdate()!=1)throw new SQLException("Tồn kho không đủ để xuất.");
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO inventory_movements(item_id,movement_type,quantity,note,created_by) VALUES(?,?,?,?,?)")){
                        p.setInt(1,id);p.setString(2,receive?"NHAP":"XUAT");p.setBigDecimal(3,amount);p.setString(4,note.getText());p.setInt(5,Session.getUserId());p.executeUpdate();
                    }
                    c.commit();
                }catch(Exception ex){c.rollback();throw ex;}
                finally{c.setAutoCommit(true);}
            }
            load();
        }catch(Exception ex){error(ex);}
    }
    private void error(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi kho",JOptionPane.ERROR_MESSAGE);}
}
