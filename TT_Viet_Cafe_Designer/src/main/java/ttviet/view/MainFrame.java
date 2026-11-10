package ttviet.view;
import javax.swing.*;import java.awt.*;
public class MainFrame extends JFrame {
 private JLabel lblBrand,lblWelcome;private JButton btnMenu,btnOrder,btnHistory,btnLogout;
 public MainFrame(){initComponents();}
 //GEN-BEGIN:initComponents
 private void initComponents(){
  setTitle("T & T Viet - Quản lý quán");setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(900,600);setLocationRelativeTo(null);getContentPane().setBackground(new Color(25,25,25));getContentPane().setLayout(null);
  lblBrand=new JLabel("T & T VIET");lblBrand.setFont(new Font("Serif",Font.BOLD,30));lblBrand.setForeground(new Color(205,160,92));lblBrand.setBounds(35,25,300,50);add(lblBrand);
  lblWelcome=new JLabel("HỆ THỐNG QUẢN LÝ CAFE - THỨC ĂN NHANH");lblWelcome.setForeground(Color.WHITE);lblWelcome.setFont(new Font("Arial",Font.BOLD,18));lblWelcome.setBounds(35,105,700,35);add(lblWelcome);
  btnMenu=make("QUẢN LÝ THỰC ĐƠN",70,190);btnOrder=make("TẠO ĐƠN / THANH TOÁN",70,260);btnHistory=make("LỊCH SỬ ĐƠN HÀNG",70,330);btnLogout=make("ĐĂNG XUẤT",70,430);
  btnMenu.addActionListener(e->new MenuFrame().setVisible(true));btnOrder.addActionListener(e->new OrderFrame().setVisible(true));btnHistory.addActionListener(e->new HistoryFrame().setVisible(true));btnLogout.addActionListener(e->{new LoginFrame().setVisible(true);dispose();});
 }
 //GEN-END:initComponents
 private JButton make(String s,int x,int y){JButton b=new JButton(s);b.setBounds(x,y,300,48);b.setBackground(new Color(169,119,58));b.setForeground(Color.WHITE);b.setFont(new Font("Arial",Font.BOLD,14));add(b);return b;}
}