package ttviet.view;
import javax.swing.*;import java.awt.*;import ttviet.dao.ProductDAO;
public class MenuFrame extends JFrame {
 //GEN-BEGIN:variables
 private JLabel lblTitle;private JTable tblProducts;private JScrollPane scroll;private JButton btnRefresh,btnClose;
 //GEN-END:variables
 public MenuFrame(){initComponents();tblProducts.setModel(ProductDAO.load());}
 //GEN-BEGIN:initComponents
 private void initComponents(){setTitle("Thực đơn - T & T Viet");setDefaultCloseOperation(DISPOSE_ON_CLOSE);setSize(950,620);setLocationRelativeTo(null);getContentPane().setLayout(null);getContentPane().setBackground(new Color(28,28,28));
 lblTitle=new JLabel("DANH SÁCH THỰC ĐƠN");lblTitle.setFont(new Font("Arial",Font.BOLD,24));lblTitle.setForeground(new Color(205,160,92));lblTitle.setBounds(30,20,500,40);add(lblTitle);
 tblProducts=new JTable();tblProducts.setRowHeight(28);scroll=new JScrollPane(tblProducts);scroll.setBounds(30,85,870,420);add(scroll);
 btnRefresh=new JButton("LÀM MỚI");btnRefresh.setBounds(650,525,120,35);add(btnRefresh);btnClose=new JButton("ĐÓNG");btnClose.setBounds(785,525,115,35);add(btnClose);
 btnRefresh.addActionListener(e->tblProducts.setModel(ProductDAO.load()));btnClose.addActionListener(e->dispose());}
 //GEN-END:initComponents
}