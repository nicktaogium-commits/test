package ttviet.view;
import javax.swing.*;import javax.swing.table.DefaultTableModel;import java.awt.*;import java.sql.*;import ttviet.dao.DB;import ttviet.dao.OrderDAO;
public class OrderFrame extends JFrame {
 //GEN-BEGIN:variables
 private JLabel lblTitle,lblTotal;private JComboBox<String> cboProduct;private JSpinner spQty;private JTable tblCart;private JScrollPane scroll;private JButton btnAdd,btnPay,btnClose;
 // Biến dữ liệu giỏ hàng do lập trình viên quản lý
 private DefaultTableModel model;
 //GEN-END:variables
 public OrderFrame(){initComponents();loadProducts();}
 //GEN-BEGIN:initComponents
 private void initComponents(){setTitle("Tạo đơn hàng - T & T Viet");setDefaultCloseOperation(DISPOSE_ON_CLOSE);setSize(950,620);setLocationRelativeTo(null);getContentPane().setLayout(null);getContentPane().setBackground(new Color(28,28,28));
 lblTitle=new JLabel("TẠO ĐƠN HÀNG");lblTitle.setForeground(new Color(205,160,92));lblTitle.setFont(new Font("Arial",Font.BOLD,24));lblTitle.setBounds(30,20,400,40);add(lblTitle);
 cboProduct=new JComboBox<>();cboProduct.setBounds(30,90,430,34);add(cboProduct);spQty=new JSpinner(new SpinnerNumberModel(1,1,99,1));spQty.setBounds(480,90,70,34);add(spQty);btnAdd=new JButton("THÊM MÓN");btnAdd.setBounds(575,90,140,34);add(btnAdd);
 model=new DefaultTableModel(new String[]{"Món","Đơn giá","SL","Thành tiền"},0);tblCart=new JTable(model);tblCart.setRowHeight(28);scroll=new JScrollPane(tblCart);scroll.setBounds(30,150,850,320);add(scroll);
 lblTotal=new JLabel("Tổng: 0 đ");lblTotal.setForeground(Color.WHITE);lblTotal.setFont(new Font("Arial",Font.BOLD,20));lblTotal.setBounds(30,490,400,40);add(lblTotal);btnPay=new JButton("THANH TOÁN");btnPay.setBounds(570,520,150,38);add(btnPay);btnClose=new JButton("ĐÓNG");btnClose.setBounds(740,520,140,38);add(btnClose);
 btnAdd.addActionListener(e->addItem());btnPay.addActionListener(e->{if(model.getRowCount()>0 && OrderDAO.save(model)){JOptionPane.showMessageDialog(this,"Thanh toán thành công!");model.setRowCount(0);lblTotal.setText("Tổng: 0 đ");}});btnClose.addActionListener(e->dispose());}
 //GEN-END:initComponents
 private void loadProducts(){try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT name,price FROM products WHERE active=1 ORDER BY name")){while(r.next())cboProduct.addItem(r.getString(1)+" | "+r.getBigDecimal(2));}catch(Exception e){JOptionPane.showMessageDialog(this,"Lỗi tải món: "+e.getMessage());}}
 private void addItem(){String v=(String)cboProduct.getSelectedItem();if(v==null)return;String[] a=v.split(" \\| ");double price=Double.parseDouble(a[1]);int q=(int)spQty.getValue();model.addRow(new Object[]{a[0],price,q,price*q});double sum=0;for(int i=0;i<model.getRowCount();i++)sum+=Double.parseDouble(model.getValueAt(i,3).toString());lblTotal.setText("Tổng: "+String.format("%,.0f",sum)+" đ");}
}