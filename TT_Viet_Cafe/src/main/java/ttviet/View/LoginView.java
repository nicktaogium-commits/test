package ttviet.View;
import javax.swing.*;
public class LoginView extends JFrame {
 public LoginView(){initComponents();}
 //GEN-BEGIN:initComponents
 @SuppressWarnings("unchecked")
 private void initComponents(){
  lblTitle=new JLabel("T & T VIET",SwingConstants.CENTER);lblUser=new JLabel("Tên đăng nhập");lblPass=new JLabel("Mật khẩu");txtUsername=new JTextField("admin");txtPassword=new JPasswordField();btnLogin=new JButton("ĐĂNG NHẬP");
  setDefaultCloseOperation(EXIT_ON_CLOSE);setTitle("Đăng nhập - T & T Viet");getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
  getContentPane().add(lblTitle,new org.netbeans.lib.awtextra.AbsoluteConstraints(80,25,340,50));getContentPane().add(lblUser,new org.netbeans.lib.awtextra.AbsoluteConstraints(75,115,120,28));getContentPane().add(txtUsername,new org.netbeans.lib.awtextra.AbsoluteConstraints(200,115,210,30));getContentPane().add(lblPass,new org.netbeans.lib.awtextra.AbsoluteConstraints(75,165,120,28));getContentPane().add(txtPassword,new org.netbeans.lib.awtextra.AbsoluteConstraints(200,165,210,30));getContentPane().add(btnLogin,new org.netbeans.lib.awtextra.AbsoluteConstraints(160,225,170,38));
  setSize(500,330);setLocationRelativeTo(null);
 }
 //GEN-END:initComponents
 //GEN-BEGIN:variables
 private JLabel lblTitle,lblUser,lblPass;private JTextField txtUsername;private JPasswordField txtPassword;private JButton btnLogin;
 //GEN-END:variables
 public JTextField getTxtUsername(){return txtUsername;}public JPasswordField getTxtPassword(){return txtPassword;}public JButton getBtnLogin(){return btnLogin;}
}