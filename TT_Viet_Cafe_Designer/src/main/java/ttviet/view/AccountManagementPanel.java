package ttviet.view;

import ttviet.dao.DB;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Quản lý tài khoản đăng nhập và gán đúng vai trò cho nhân viên. */
public class AccountManagementPanel extends JPanel {
    private final JTable grid=new JTable(new DefaultTableModel(new Object[]{"Mã","Tên đăng nhập","Họ tên","Vai trò","Nhân viên liên kết"},0){
        @Override public boolean isCellEditable(int r,int c){return false;}
    });
    public AccountManagementPanel(){
        setLayout(new BorderLayout(10,10));setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JLabel title=new JLabel("TÀI KHOẢN & PHÂN QUYỀN");
        title.setFont(new Font("SansSerif",Font.BOLD,20));title.setForeground(new Color(30,112,166));
        JButton add=new JButton("Tạo tài khoản"),reset=new JButton("Đặt lại mật khẩu"),refresh=new JButton("Làm mới");
        JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);top.add(title,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));actions.setOpaque(false);actions.add(add);actions.add(reset);actions.add(refresh);
        add(top,BorderLayout.NORTH);add(new JScrollPane(grid),BorderLayout.CENTER);add(actions,BorderLayout.SOUTH);
        add.addActionListener(e->create());reset.addActionListener(e->resetPassword());refresh.addActionListener(e->load());
        grid.setRowHeight(28);load();
    }
    private void load(){
        DefaultTableModel m=(DefaultTableModel)grid.getModel();m.setRowCount(0);
        String sql="SELECT u.id,u.username,u.full_name,u.role,COALESCE(e.full_name,'—') FROM users u LEFT JOIN employees e ON e.id=u.employee_id ORDER BY u.id";
        try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){
            while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),roleName(r.getString(4)),r.getString(5)});
        }catch(Exception ex){error(ex);}
    }
    private String roleName(String role){
        if("manager".equals(role))return "Quản lý";if("cashier".equals(role))return "Thu ngân";
        if("service".equals(role))return "Phục vụ";if("kitchen".equals(role))return "Bếp";if("warehouse".equals(role))return "Kho";
        return role;
    }
    private void create(){
        JTextField username=new JTextField(),password=new JPasswordField(),fullName=new JTextField();
        JComboBox<String> role=new JComboBox<>(new String[]{"cashier","service","kitchen","warehouse","manager"});
        JComboBox<String> employee=new JComboBox<>();java.util.Map<String,Integer> ids=new java.util.LinkedHashMap<>();
        try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,full_name FROM employees WHERE active=1 ORDER BY full_name")){
            while(r.next()){ids.put(r.getString(2),r.getInt(1));employee.addItem(r.getString(2));}
        }catch(Exception ex){error(ex);return;}
        JPanel f=new JPanel(new GridLayout(0,1,4,4));
        f.add(new JLabel("Tên đăng nhập"));f.add(username);f.add(new JLabel("Mật khẩu khởi tạo"));f.add(password);
        f.add(new JLabel("Họ tên hiển thị"));f.add(fullName);f.add(new JLabel("Vai trò"));f.add(role);
        f.add(new JLabel("Nhân viên liên kết (có thể bỏ trống)"));f.add(employee);
        if(JOptionPane.showConfirmDialog(this,f,"Tạo tài khoản",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        String u=username.getText().trim(),pw=new String(((JPasswordField)password).getPassword()),name=fullName.getText().trim();
        if(u.isEmpty()||pw.isEmpty()||name.isEmpty()){JOptionPane.showMessageDialog(this,"Nhập đầy đủ tên đăng nhập, mật khẩu và họ tên.");return;}
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO users(username,password,full_name,role,employee_id) VALUES(?,?,?,?,?)")){
            p.setString(1,u);p.setString(2,pw);p.setString(3,name);p.setString(4,(String)role.getSelectedItem());
            Integer employeeId=ids.get(employee.getSelectedItem());if(employeeId==null)p.setNull(5,Types.INTEGER);else p.setInt(5,employeeId);
            p.executeUpdate();load();
        }catch(Exception ex){error(ex);}
    }
    private void resetPassword(){
        int row=grid.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Chọn tài khoản cần đổi mật khẩu.");return;}
        int id=((Number)grid.getValueAt(row,0)).intValue();JPasswordField p=new JPasswordField();
        if(JOptionPane.showConfirmDialog(this,p,"Nhập mật khẩu mới",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        String value=new String(p.getPassword());if(value.isEmpty())return;
        try(Connection c=DB.getConnection();PreparedStatement q=c.prepareStatement("UPDATE users SET password=? WHERE id=?")){
            q.setString(1,value);q.setInt(2,id);q.executeUpdate();JOptionPane.showMessageDialog(this,"Đã cập nhật mật khẩu."); 
        }catch(Exception ex){error(ex);}
    }
    private void error(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Lỗi tài khoản",JOptionPane.ERROR_MESSAGE);}
}
