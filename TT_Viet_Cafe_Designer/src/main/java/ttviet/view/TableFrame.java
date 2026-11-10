package ttviet.view;import javax.swing.*;import java.awt.*;import ttviet.dao.TableDAO;
public class TableFrame extends JFrame{
 //GEN-BEGIN:variables
 private JLabel lblTitle;private JTable tblTables;private JScrollPane scroll;private JButton btnRefresh,btnClose;
 //GEN-END:variables
 public TableFrame(){initComponents();tblTables.setModel(TableDAO.load());}
 //GEN-BEGIN:initComponents
 private void initComponents(){setTitle("Quản lý bàn - T & T Viet");setDefaultCloseOperation(DISPOSE_ON_CLOSE);setSize(760,520);setLocationRelativeTo(null);getContentPane().setLayout(null);getContentPane().setBackground(new Color(28,28,28));
 lblTitle=new JLabel("QUẢN LÝ BÀN / KHU VỰC");lblTitle.setForeground(new Color(205,160,92));lblTitle.setFont(new Font("Arial",Font.BOLD,23));lblTitle.setBounds(25,20,500,40);add(lblTitle);
 tblTables=new JTable();tblTables.setRowHeight(28);scroll=new JScrollPane(tblTables);scroll.setBounds(25,85,690,310);add(scroll);
 btnRefresh=new JButton("LÀM MỚI");btnRefresh.setBounds(455,425,120,35);add(btnRefresh);btnClose=new JButton("ĐÓNG");btnClose.setBounds(595,425,120,35);add(btnClose);btnRefresh.addActionListener(e->tblTables.setModel(TableDAO.load()));btnClose.addActionListener(e->dispose());}
 //GEN-END:initComponents
}