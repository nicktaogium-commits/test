package ttviet.view;
import javax.swing.*;import java.awt.*;import ttviet.dao.AuthDAO;
public class LoginFrame extends JFrame {
 //GEN-BEGIN:variables
 private JLabel lblTitle,lblUser,lblPass;private JTextField txtUsername;private JPasswordField txtPassword;private JButton btnLogin,btnExit;
 //GEN-END:variables
 public LoginFrame(){initComponents();}
 //GEN-BEGIN:initComponents
 private void initComponents(){
  setTitle("Đăng nhập - T & T Viet");setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(560,390);setLocationRelativeTo(null);setResizable(false);getContentPane().setBackground(new Color(242,248,252));getContentPane().setLayout(null);
  lblTitle=new JLabel("T & T VIET",SwingConstants.CENTER);lblTitle.setFont(new Font("Serif",Font.BOLD,34));lblTitle.setForeground(new Color(30,112,166));lblTitle.setBounds(80,30,400,55);add(lblTitle);
  lblUser=new JLabel("Tên đăng nhập");lblUser.setForeground(new Color(38,55,68));lblUser.setBounds(95,145,130,28);add(lblUser);
  txtUsername=new JTextField("admin");txtUsername.setBounds(230,145,230,32);add(txtUsername);
  lblPass=new JLabel("Mật khẩu");lblPass.setForeground(new Color(38,55,68));lblPass.setBounds(95,195,130,28);add(lblPass);
  txtPassword=new JPasswordField();txtPassword.setBounds(230,195,230,32);add(txtPassword);
  btnLogin=new JButton("ĐĂNG NHẬP");btnLogin.setBounds(155,260,150,40);btnLogin.setBackground(new Color(30,112,166));btnLogin.setForeground(Color.WHITE);add(btnLogin);btnExit=new JButton("THOÁT");btnExit.setBounds(320,260,110,40);btnExit.setBackground(new Color(225,241,250));btnExit.setForeground(new Color(25,91,135));add(btnExit);
  btnLogin.addActionListener(e->{if(AuthDAO.login(txtUsername.getText().trim(),new String(txtPassword.getPassword()))){new OperationsFrame().setVisible(true);dispose();}else JOptionPane.showMessageDialog(this,"Sai tài khoản hoặc mật khẩu.");});btnExit.addActionListener(e->System.exit(0));getRootPane().setDefaultButton(btnLogin);
 }
 //GEN-END:initComponents
}