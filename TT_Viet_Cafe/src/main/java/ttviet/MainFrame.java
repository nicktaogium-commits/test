package ttviet;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {
    private final int userId;
    private final String fullName, role;
    private JTable menuTable, historyTable, tableTable;
    private DefaultTableModel menuModel, historyModel, tableModel;
    private JComboBox<String> productBox, tableBox;
    private JSpinner qty;
    private JLabel totalLabel;

    public MainFrame(int id, String name, String userRole) {
        userId = id; fullName = name; role = userRole;
        initComponents();
        buildRuntimeTabs();
        loadProducts(); loadHistory(); loadTables();
    }

    //GEN-BEGIN:initComponents
    @SuppressWarnings("unchecked")
    private void initComponents() {
        lblBrand = new javax.swing.JLabel();
        tabs = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("T & T Viet - Quản lý quán");
        getContentPane().setLayout(null);

        lblBrand.setText("T & T VIET  |  QUẢN LÝ QUÁN");
        lblBrand.setFont(new java.awt.Font("Serif", java.awt.Font.BOLD, 25));
        getContentPane().add(lblBrand);
        lblBrand.setBounds(22, 14, 480, 40);

        getContentPane().add(tabs);
        tabs.setBounds(16, 70, 1080, 570);

        setSize(new java.awt.Dimension(1120, 720));
        setLocationRelativeTo(null);
    }
    //GEN-END:initComponents

    //GEN-BEGIN:variables
    private javax.swing.JLabel lblBrand;
    private javax.swing.JTabbedPane tabs;
    //GEN-END:variables

    private void buildRuntimeTabs() {
        getContentPane().setBackground(Theme.BG);
        lblBrand.setForeground(Theme.GOLD);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel who = new JLabel("Xin chào, " + fullName + "  (" + role + ")");
        who.setForeground(Theme.TEXT);
        who.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(who, BorderLayout.CENTER);
        header.setBounds(520, 14, 560, 40);
        getContentPane().add(header);

        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("Thực đơn", buildMenuPanel());
        tabs.addTab("Tạo hóa đơn", buildOrderPanel());
        tabs.addTab("Lịch sử hóa đơn", buildHistoryPanel());
        tabs.addTab("Bàn", buildTablesPanel());

        JButton logout = Theme.button("Đăng xuất");
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        foot.setBackground(Theme.BG); foot.add(logout);
        foot.setBounds(850, 645, 230, 35);
        getContentPane().add(foot);
    }

    private JPanel panel() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        return p;
    }
    private JTable makeTable(DefaultTableModel m) { JTable t = new JTable(m); Theme.table(t); return t; }

    private JPanel buildMenuPanel() {
        JPanel p=panel(); JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT)); top.setBackground(Theme.BG);
        JButton refresh=Theme.button("Làm mới"); refresh.addActionListener(e->loadProducts()); top.add(refresh); p.add(top,BorderLayout.NORTH);
        menuModel=new DefaultTableModel(new Object[]{"Mã","Tên món","Danh mục","Giá (VNĐ)"},0){public boolean isCellEditable(int r,int c){return false;}};
        menuTable=makeTable(menuModel); p.add(new JScrollPane(menuTable),BorderLayout.CENTER); return p;
    }
    private JPanel buildOrderPanel() {
        JPanel p=panel(); JPanel form=new JPanel(new GridLayout(0,2,12,12)); form.setBackground(Theme.PANEL); form.setBorder(BorderFactory.createEmptyBorder(22,22,22,22));
        productBox=new JComboBox<>(); qty=new JSpinner(new SpinnerNumberModel(1,1,99,1)); tableBox=new JComboBox<>();
        form.add(label("Chọn món")); form.add(productBox); form.add(label("Số lượng")); form.add(qty); form.add(label("Bàn / hình thức")); form.add(tableBox);
        JButton add=Theme.button("Thêm món vào hóa đơn"); JButton save=Theme.button("Lưu hóa đơn");
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setBackground(Theme.BG); actions.add(add); actions.add(save);
        DefaultTableModel cartModel=new DefaultTableModel(new Object[]{"Món","Đơn giá","SL","Thành tiền"},0);
        JTable cart=new JTable(cartModel); Theme.table(cart); totalLabel=label("Tổng: 0 ₫");
        add.addActionListener(e->{ Object selected=productBox.getSelectedItem(); if(selected==null)return; String[] a=selected.toString().split(" \\| "); int count=(Integer)qty.getValue(); long price=Long.parseLong(a[2]); cartModel.addRow(new Object[]{a[1],price,count,price*count}); updateTotal(cartModel); });
        save.addActionListener(e->saveOrder(cartModel));
        JPanel north=new JPanel(new BorderLayout(8,8)); north.setBackground(Theme.BG); north.add(form,BorderLayout.CENTER); north.add(actions,BorderLayout.SOUTH);
        p.add(north,BorderLayout.NORTH); p.add(new JScrollPane(cart),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setBackground(Theme.BG); totalLabel.setForeground(Theme.GOLD); totalLabel.setFont(new Font("Segoe UI",Font.BOLD,20)); bottom.add(totalLabel); p.add(bottom,BorderLayout.SOUTH);
        return p;
    }
    private JLabel label(String s) { JLabel l=new JLabel(s); l.setForeground(Theme.TEXT); return l; }
    private void updateTotal(DefaultTableModel m) { long total=0; for(int i=0;i<m.getRowCount();i++) total+=(long)m.getValueAt(i,3); totalLabel.setText("Tổng: "+String.format("%,d",total)+" ₫"); }
    private void saveOrder(DefaultTableModel cart) {
        if(cart.getRowCount()==0){JOptionPane.showMessageDialog(this,"Chưa có món trong hóa đơn.");return;}
        String code="TTV"+System.currentTimeMillis(); long total=0; for(int i=0;i<cart.getRowCount();i++) total+=(long)cart.getValueAt(i,3);
        try(Connection c=DB.getConnection()) {
            c.setAutoCommit(false);
            try(PreparedStatement h=c.prepareStatement("INSERT INTO orders(order_code,total,user_id) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                h.setString(1,code); h.setLong(2,total); h.setInt(3,userId); h.executeUpdate();
                try(ResultSet keys=h.getGeneratedKeys()) { keys.next(); long orderId=keys.getLong(1);
                    try(PreparedStatement d=c.prepareStatement("INSERT INTO order_items(order_id,product_id,quantity,unit_price) SELECT ?,id,?,price FROM products WHERE name=?")) {
                        for(int i=0;i<cart.getRowCount();i++){d.setLong(1,orderId);d.setInt(2,(int)cart.getValueAt(i,2));d.setString(3,(String)cart.getValueAt(i,0));d.addBatch();} d.executeBatch();
                    }
                }
            }
            c.commit(); cart.setRowCount(0); updateTotal(cart); loadHistory(); JOptionPane.showMessageDialog(this,"Đã lưu hóa đơn "+code);
        } catch(SQLException ex) { JOptionPane.showMessageDialog(this,"Không lưu được hóa đơn: "+ex.getMessage()); }
    }
    private JPanel buildHistoryPanel() {
        JPanel p=panel(); JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT)); top.setBackground(Theme.BG); JButton refresh=Theme.button("Làm mới"); refresh.addActionListener(e->loadHistory()); top.add(refresh); p.add(top,BorderLayout.NORTH);
        historyModel=new DefaultTableModel(new Object[]{"Mã hóa đơn","Thời gian","Tổng tiền","Nhân viên"},0); historyTable=makeTable(historyModel); p.add(new JScrollPane(historyTable),BorderLayout.CENTER); return p;
    }
    private JPanel buildTablesPanel() {
        JPanel p=panel(); JButton refresh=Theme.button("Làm mới trạng thái bàn"); refresh.addActionListener(e->loadTables()); JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT)); top.setBackground(Theme.BG); top.add(refresh); p.add(top,BorderLayout.NORTH);
        tableModel=new DefaultTableModel(new Object[]{"Mã","Tên bàn","Trạng thái"},0); tableTable=makeTable(tableModel); p.add(new JScrollPane(tableTable),BorderLayout.CENTER); return p;
    }
    private void loadProducts() {
        if(menuModel==null)return; menuModel.setRowCount(0); if(productBox!=null)productBox.removeAllItems();
        try(Connection c=DB.getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT id,name,category,price FROM products WHERE active=1 ORDER BY category,name")) {
            while(r.next()){int id=r.getInt(1);String name=r.getString(2);String cat=r.getString(3);long price=r.getLong(4);menuModel.addRow(new Object[]{id,name,cat,String.format("%,d",price)});if(productBox!=null)productBox.addItem(id+" | "+name+" | "+price);}
        } catch(SQLException ex) { JOptionPane.showMessageDialog(this,"Lỗi tải thực đơn: "+ex.getMessage()); }
    }
    private void loadHistory() {
        if(historyModel==null)return; historyModel.setRowCount(0);
        try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT o.order_code,o.created_at,o.total,u.full_name FROM orders o LEFT JOIN users u ON o.user_id=u.id ORDER BY o.id DESC")) { while(r.next())historyModel.addRow(new Object[]{r.getString(1),r.getTimestamp(2),String.format("%,d",r.getLong(3)),r.getString(4)}); }
        catch(SQLException ex) { JOptionPane.showMessageDialog(this,"Lỗi tải lịch sử: "+ex.getMessage()); }
    }
    private void loadTables() {
        if(tableModel==null)return; tableModel.setRowCount(0); if(tableBox!=null)tableBox.removeAllItems();
        try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,table_name,status FROM cafe_tables ORDER BY id")) { while(r.next()){tableModel.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3)});if(tableBox!=null)tableBox.addItem(r.getString(2));} }
        catch(SQLException ex) { JOptionPane.showMessageDialog(this,"Lỗi tải bàn: "+ex.getMessage()); }
    }
}
