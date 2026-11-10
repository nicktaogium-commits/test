package ttviet.View;
import ttviet.Model.User;import javax.swing.*;import java.awt.*;
public class MainView extends JFrame {
 public MainView(User user){initComponents();lblWelcome.setText("Xin chào: "+user.getFullName());}
 //GEN-BEGIN:initComponents
 private void initComponents(){
  lblBrand=new JLabel("T & T VIET - QUẢN LÝ QUÁN");lblWelcome=new JLabel("Xin chào");tabs=new JTabbedPane();btnLogout=new JButton("Đăng xuất");
  setDefaultCloseOperation(EXIT_ON_CLOSE);setTitle("T & T Viet");getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
  getContentPane().add(lblBrand,new org.netbeans.lib.awtextra.AbsoluteConstraints(20,15,420,40));getContentPane().add(lblWelcome,new org.netbeans.lib.awtextra.AbsoluteConstraints(650,20,300,30));getContentPane().add(tabs,new org.netbeans.lib.awtextra.AbsoluteConstraints(15,65,1080,580));getContentPane().add(btnLogout,new org.netbeans.lib.awtextra.AbsoluteConstraints(950,650,130,30));
  tabs.addTab("Thực đơn",new JPanel());tabs.addTab("Tạo hóa đơn",new JPanel());tabs.addTab("Lịch sử hóa đơn",new JPanel());tabs.addTab("Bàn",new JPanel());setSize(1120,730);setLocationRelativeTo(null);
 }
 //GEN-END:initComponents
 //GEN-BEGIN:variables
 private JLabel lblBrand,lblWelcome;private JTabbedPane tabs;private JButton btnLogout;
 //GEN-END:variables
 public JButton getBtnLogout(){return btnLogout;}
}