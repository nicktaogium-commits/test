package ttviet.view;
import javax.swing.*;import java.awt.*;
public class MainFrame extends JFrame {
 //GEN-BEGIN:variables
 private JLabel lblBrand,lblWelcome;private JButton btnMenu,btnOrder,btnHistory,btnTables,btnLogout;
 //GEN-END:variables
 public MainFrame(){initComponents();}
 //GEN-BEGIN:initComponents
 private void initComponents(){
  setTitle("T & T Viet - Quản lý quán");setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(900,600);setLocationRelativeTo(null);getContentPane().setBackground(new Color(25,25,25));getContentPane().setLayout(null);
  lblBrand=new JLabel("T & T VIET");lblBrand.setFont(new Font("Serif",Font.BOLD,30));lblBrand.setForeground(new Color(205,160,92));lblBrand.setBounds(35,25,300,50);add(lblBrand);
  lblWelcome=new JLabel("HỆ THỐNG QUẢN LÝ CAFE - THỨC ĂN NHANH");lblWelcome.setForeground(Color.WHITE);lblWelcome.setFont(new Font("Arial",Font.BOLD,18));lblWelcome.setBounds(35,105,700,35);add(lblWelcome);
  btnMenu=new JButton("QUẢN LÝ THỰC ĐƠN");btnMenu.setBounds(70,190,300,48);add(btnMenu);
  btnOrder=new JButton("TẠO ĐƠN / THANH TOÁN");btnOrder.setBounds(70,260,300,48);add(btnOrder);
  btnHistory=new JButton("LỊCH SỬ ĐƠN HÀNG");btnHistory.setBounds(70,330,300,48);add(btnHistory);
  btnTables=new JButton("QUẢN LÝ BÀN / KHU VỰC");btnTables.setBounds(70,400,300,48);add(btnTables);
  btnLogout=new JButton("ĐĂNG XUẤT");btnLogout.setBounds(70,470,300,48);add(btnLogout);
  for(JButton b:new JButton[]{btnMenu,btnOrder,btnHistory,btnTables,btnLogout}){b.setBackground(new Color(169,119,58));b.setForeground(Color.WHITE);b.setFont(new Font("Arial",Font.BOLD,14));}
  btnMenu.addActionListener(e->new MenuFrame().setVisible(true));btnOrder.addActionListener(e->new OrderFrame().setVisible(true));btnHistory.addActionListener(e->new HistoryFrame().setVisible(true));btnTables.addActionListener(e->new TableFrame().setVisible(true));btnLogout.addActionListener(e->{new LoginFrame().setVisible(true);dispose();});
 }
 //GEN-END:initComponents
}