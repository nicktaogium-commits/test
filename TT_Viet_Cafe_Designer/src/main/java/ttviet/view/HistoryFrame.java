package ttviet.view;
import javax.swing.*;import java.awt.*;import ttviet.dao.HistoryDAO;
public class HistoryFrame extends JFrame {
 //GEN-BEGIN:variables
 private JLabel lblTitle;private JTable tblHistory;private JScrollPane scroll;private JButton btnClose;
 //GEN-END:variables
 public HistoryFrame(){initComponents();}
 //GEN-BEGIN:initComponents
 private void initComponents(){setTitle("Lịch sử đơn hàng");setDefaultCloseOperation(DISPOSE_ON_CLOSE);setSize(900,560);setLocationRelativeTo(null);getContentPane().setLayout(null);getContentPane().setBackground(new Color(28,28,28));
 lblTitle=new JLabel("LỊCH SỬ ĐƠN HÀNG");lblTitle.setForeground(new Color(205,160,92));lblTitle.setFont(new Font("Arial",Font.BOLD,24));lblTitle.setBounds(30,20,500,40);add(lblTitle);
 tblHistory=new JTable(HistoryDAO.load());scroll=new JScrollPane(tblHistory);scroll.setBounds(30,85,820,360);add(scroll);
 btnClose=new JButton("ĐÓNG");btnClose.setBounds(735,470,115,35);add(btnClose);btnClose.addActionListener(e->dispose());}
 //GEN-END:initComponents
}