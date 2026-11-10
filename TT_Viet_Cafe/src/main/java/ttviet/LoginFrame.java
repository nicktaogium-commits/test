package ttviet;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
public class LoginFrame extends JFrame {
 private JTextField txtUser;
 private JPasswordField txtPass;
 private JButton btnLogin;
 public LoginFrame() { initComponents(); }
 private void initComponents() {
  setTitle("Đăng nhập - T & T Viet"); setDefaultCloseOperation(EXIT_ON_CLOSE);
  setSize(520,360); setLocationRelativeTo(null); setResizable(false);
  getContentPane().setBackground(Theme.BG); setLayout(null);
  JLabel title=new JLabel("T & T VIET",SwingConstants.CENTER); title.setBounds(90,28,340,48);
  title.setFont(new Font("Serif",Font.BOLD,32)); title.setForeground(Theme.GOLD); add(title);
  JLabel sub=new JLabel("QUẢN LÝ QUÁN CÀ PHÊ – THỨC ĂN NHANH",SwingConstants.CENTER);
  sub.setBounds(50,78,420,25); sub.setForeground(Theme.TEXT); add(sub);
  JLabel u=new JLabel("Tên đăng nhập"); u.setBounds(95,130,120,25); u.setForeground(Theme.TEXT); add(u);
  txtUser=new JTextField("admin"); txtUser.setBounds(220,130,205,30); add(txtUser);
  JLabel p=new JLabel("Mật khẩu"); p.setBounds(95,177,120,25); p.setForeground(Theme.TEXT); add(p);
  txtPass=new JPasswordField(); txtPass.setBounds(220,177,205,30); add(txtPass);
  btnLogin=Theme.button("ĐĂNG NHẬP"); btnLogin.setBounds(165,235,190,38); add(btnLogin);
  btnLogin.addActionListener(e -> login()); txtPass.addActionListener(e -> login());
 }
 private void login() {
  String sql="SELECT id,full_name,role FROM users WHERE username=? AND password=?";
  try(Connection c=DB.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
   p.setString(1,txtUser.getText().trim()); p.setString(2,new String(txtPass.getPassword()));
   try(ResultSet r=p.executeQuery()) {
    if(r.next()) { new MainFrame(r.getInt("id"),r.getString("full_name"),r.getString("role")).setVisible(true); dispose(); }
    else JOptionPane.showMessageDialog(this,"Sai tài khoản hoặc mật khẩu.");
   }
  } catch(SQLException ex) { JOptionPane.showMessageDialog(this,"Không kết nối được MySQL. Hãy import database/tt_viet.sql.\n"+ex.getMessage(),"Lỗi cơ sở dữ liệu",JOptionPane.ERROR_MESSAGE); }
 }
}