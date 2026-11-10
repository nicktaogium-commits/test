package ttviet.view;

import ttviet.dao.DB;
import ttviet.dao.Session;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Trung tâm nghiệp vụ T&T Viet. Các thao tác dữ liệu thực hiện trực tiếp trên MySQL.
 * Giao diện có thể chạy độc lập; các JFrame GUI Builder hiện có vẫn được giữ nguyên.
 */
public class OperationsFrame extends JFrame {
    private static final Color BG=new Color(24,25,27), PANEL=new Color(37,38,41),
        GOLD=new Color(205,160,92), TEXT=new Color(245,239,226), MUTED=new Color(180,172,157);
    private final JTabbedPane tabs=new JTabbedPane();
    private JTable productTable,tableTable,staffTable,shiftTable,damageTable,historyTable;
    private JLabel revenueValue,ordersValue,tablesValue;
    private final NumberFormat money=NumberFormat.getNumberInstance(new Locale("vi","VN"));

    public OperationsFrame(){
        setTitle("T & T VIET | Trung tâm quản lý"); setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280,820); setMinimumSize(new Dimension(1050,700)); setLocationRelativeTo(null);
        getContentPane().setBackground(BG); setLayout(new BorderLayout());
        add(header(),BorderLayout.NORTH); add(tabs,BorderLayout.CENTER);
        tabs.addTab("Tổng quan",dashboard()); tabs.addTab("Thực đơn & giá",products());
        tabs.addTab("Sơ đồ bàn",tables()); tabs.addTab("Nhân viên",staff());
        tabs.addTab("Phân ca",shifts()); tabs.addTab("Bồi thường",damages());
        tabs.addTab("Hóa đơn",history()); tabs.addTab("Báo cáo",reports());
        styleTabs(); applyRolePermissions(); refreshAll();
    }
    private JPanel header(){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(new Color(17,18,19));p.setBorder(new EmptyBorder(15,24,15,24));
        JLabel brand=new JLabel("<html><span style='color:#CDA05C;font-size:25pt;font-weight:bold'>T &amp; T VIET</span><br><span style='color:#D9C9A9'>CAFE • FAST FOOD &nbsp; | &nbsp; HỆ THỐNG QUẢN LÝ</span></html>");
        p.add(brand,BorderLayout.WEST);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,5));right.setOpaque(false);
        String roleLabel=roleLabel();
        JLabel user=new JLabel(Session.getFullName()+"  •  "+roleLabel,SwingConstants.RIGHT);user.setForeground(GOLD);
        JButton logout=button("ĐĂNG XUẤT");
        logout.addActionListener(e->{
            int answer=JOptionPane.showConfirmDialog(this,"Bạn có chắc muốn đăng xuất?","Xác nhận đăng xuất",JOptionPane.YES_NO_OPTION);
            if(answer==JOptionPane.YES_OPTION){Session.clear();dispose();new LoginFrame().setVisible(true);}
        });
        right.add(user);right.add(logout);p.add(right,BorderLayout.EAST);
        return p;
    }
    private String normalizedRole(){
        return Session.getRole()==null?"":Session.getRole().trim().toLowerCase(java.util.Locale.ROOT);
    }
    private boolean isManager(){
        String r=normalizedRole();
        return r.equals("manager")||r.equals("admin")||r.equals("quản lý")||r.equals("quan ly");
    }
    private boolean isCashier(){
        String r=normalizedRole();
        return r.equals("cashier")||r.equals("thu ngân")||r.equals("thu ngan");
    }
    private boolean isService(){
        String r=normalizedRole();
        return r.equals("staff")||r.equals("service")||r.equals("phục vụ")||r.equals("phuc vu");
    }
    private boolean isKitchen(){
        String r=normalizedRole();
        return r.equals("warehouse")||r.equals("kitchen")||r.equals("bếp")||r.equals("bep");
    }
    private String roleLabel(){
        if(isManager())return "QUẢN LÝ";
        if(isCashier())return "THU NGÂN";
        if(isService())return "PHỤC VỤ";
        if(isKitchen())return "BẾP";
        return "CHƯA PHÂN QUYỀN";
    }
    private void applyRolePermissions(){
        if(isManager())return;
        java.util.Set<String> allowed=new java.util.HashSet<>();
        if(isCashier())java.util.Collections.addAll(allowed,"Tổng quan","Sơ đồ bàn","Hóa đơn","Báo cáo");
        else if(isService())java.util.Collections.addAll(allowed,"Sơ đồ bàn","Thực đơn & giá");
        else if(isKitchen())java.util.Collections.addAll(allowed,"Thực đơn & giá");
        else allowed.add("Tổng quan");
        for(int i=tabs.getTabCount()-1;i>=0;i--)
            if(!allowed.contains(tabs.getTitleAt(i)))tabs.removeTabAt(i);

        // Nhân viên phục vụ và bếp chỉ được xem thực đơn, không được sửa giá/món.
        if((isService()||isKitchen())&&tabs.indexOfTab("Thực đơn & giá")>=0){
            Component page=tabs.getComponentAt(tabs.indexOfTab("Thực đơn & giá"));
            if(page instanceof JPanel){
                Component south=((JPanel)page).getLayout() instanceof BorderLayout
                    ?((BorderLayout)((JPanel)page).getLayout()).getLayoutComponent(BorderLayout.SOUTH):null;
                if(south!=null)south.setVisible(false);
            }
        }
    }
    private void styleTabs(){tabs.setBackground(PANEL);tabs.setForeground(TEXT);tabs.setFont(new Font("SansSerif",Font.BOLD,13));}
    private JPanel page(String title,String desc){
        JPanel p=new JPanel(new BorderLayout(12,12));p.setBackground(BG);p.setBorder(new EmptyBorder(20,22,20,22));
        JPanel top=new JPanel(new GridLayout(2,1,2,2));top.setOpaque(false);
        JLabel t=new JLabel(title);t.setFont(new Font("SansSerif",Font.BOLD,23));t.setForeground(GOLD);
        JLabel d=new JLabel(desc);d.setForeground(MUTED);top.add(t);top.add(d);p.add(top,BorderLayout.NORTH);return p;
    }
    private JPanel card(String title,JLabel value){
        JPanel p=new JPanel(new BorderLayout(5,12));p.setBackground(PANEL);p.setBorder(new CompoundBorder(new LineBorder(new Color(92,72,43),1,true),new EmptyBorder(18,20,18,20)));
        JLabel l=new JLabel(title);l.setForeground(MUTED);l.setFont(new Font("SansSerif",Font.BOLD,12));value.setForeground(GOLD);value.setFont(new Font("SansSerif",Font.BOLD,27));p.add(l,BorderLayout.NORTH);p.add(value,BorderLayout.CENTER);return p;
    }
    private JPanel dashboard(){
        JPanel p=page("TỔNG QUAN HOẠT ĐỘNG","Theo dõi tình hình kinh doanh và truy cập nhanh các nghiệp vụ.");
        JPanel metrics=new JPanel(new GridLayout(1,3,14,14));metrics.setOpaque(false);
        revenueValue=big("0 đ");ordersValue=big("0");tablesValue=big("0");
        metrics.add(card("DOANH THU HÔM NAY",revenueValue));metrics.add(card("HÓA ĐƠN HÔM NAY",ordersValue));metrics.add(card("BÀN ĐANG CÓ KHÁCH",tablesValue));
        JPanel shortcuts=new JPanel(new GridLayout(2,4,12,12));shortcuts.setOpaque(false);
        String[] names={"Gọi món / Thanh toán","Quản lý thực đơn","Sơ đồ bàn","Nhân viên","Phân ca làm","Bồi thường tài sản","Lịch sử hóa đơn","Báo cáo doanh thu"};
        String[] targetTitles={"","Thực đơn & giá","Sơ đồ bàn","Nhân viên","Phân ca","Bồi thường","Hóa đơn","Báo cáo"};
        for(int i=0;i<names.length;i++){
            final int item=i;
            JButton b=button(names[i]);
            b.addActionListener(e->{
                if(item==0){new OrderFrame().setVisible(true);return;}
                for(int j=0;j<tabs.getTabCount();j++)
                    if(tabs.getTitleAt(j).equals(targetTitles[item])){tabs.setSelectedIndex(j);return;}
            });
            b.setName("shortcut_"+i);
            b.setVisible(isManager() || (isCashier() && (i==0||i==2||i==6||i==7)));
            shortcuts.add(b);
        }
        JPanel center=new JPanel(new BorderLayout(12,20));center.setOpaque(false);center.add(metrics,BorderLayout.NORTH);center.add(shortcuts,BorderLayout.CENTER);
        JLabel note=new JLabel("<html><div style='color:#D9C9A9;padding:18px'><h2>VẬN HÀNH T &amp; T VIET</h2><p>• Theo dõi bàn theo trạng thái: xanh - trống, đỏ - đang phục vụ, vàng - đặt trước.</p><p>• Quản lý món, giá, tồn trạng thái còn/hết; tạo đơn và thanh toán.</p><p>• Quản lý nhân viên, ca làm, tài sản hư hỏng và báo cáo doanh thu.</p></div></html>");
        note.setOpaque(true);note.setBackground(PANEL);note.setBorder(new LineBorder(new Color(92,72,43),1,true));
        p.add(center,BorderLayout.CENTER);p.add(note,BorderLayout.SOUTH);return p;
    }
    private JLabel big(String s){JLabel l=new JLabel(s);return l;}
    private JButton button(String s){JButton b=new JButton(s);b.setFocusPainted(false);b.setBackground(new Color(92,66,34));b.setForeground(TEXT);b.setFont(new Font("SansSerif",Font.BOLD,13));b.setBorder(new CompoundBorder(new LineBorder(GOLD,1,true),new EmptyBorder(14,10,14,10)));return b;}
    private JTable table(String... cols){JTable t=new JTable(new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}});t.setRowHeight(29);t.setFont(new Font("SansSerif",Font.PLAIN,13));t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,13));t.setSelectionBackground(new Color(105,78,42));return t;}
    private JScrollPane scroll(JTable t){JScrollPane s=new JScrollPane(t);s.getViewport().setBackground(PANEL);s.setBorder(new LineBorder(new Color(80,68,49)));return s;}
    private JPanel actions(Component... items){JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,9,4));p.setOpaque(false);for(Component item:items){if(item instanceof JLabel){item.setForeground(MUTED);}p.add(item);}return p;}
    private JPanel products(){
        JPanel p=page("THỰC ĐƠN & GIÁ","Thêm, sửa, xóa món; tìm theo tên; bật/tắt tình trạng còn hàng.");
        productTable=table("Mã","Tên món","Nhóm món","Giá (VNĐ)","Trạng thái");
        JTextField search=new JTextField(18);JButton find=button("Tìm kiếm"),add=button("Thêm món"),edit=button("Sửa món"),del=button("Xóa món"),toggle=button("Còn / Hết"),reload=button("Làm mới");
        find.addActionListener(e->loadProducts(search.getText()));reload.addActionListener(e->loadProducts(""));
        add.addActionListener(e->productDialog(null));edit.addActionListener(e->productDialog(productTable.getSelectedRow()));del.addActionListener(e->deleteSelected(productTable,"products"));toggle.addActionListener(e->toggleProduct());
        p.add(actions(new JLabel("Tìm món:"),search,find,add,edit,del,toggle,reload),BorderLayout.SOUTH);p.add(scroll(productTable),BorderLayout.CENTER);return p;
    }
    private void productDialog(Integer row){
        String name="",price="";int id=-1;
        if(row!=null){int r=productTable.convertRowIndexToModel(row);id=(int)productTable.getModel().getValueAt(r,0);name=String.valueOf(productTable.getModel().getValueAt(r,1));price=String.valueOf(productTable.getModel().getValueAt(r,3));}
        JTextField n=new JTextField(name),v=new JTextField(price);JComboBox<String> cat=new JComboBox<>();
        try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet rs=s.executeQuery("SELECT name FROM categories ORDER BY name")){while(rs.next())cat.addItem(rs.getString(1));}catch(Exception ex){error(ex);}
        JPanel f=new JPanel(new GridLayout(0,1,4,4));f.add(new JLabel("Tên món"));f.add(n);f.add(new JLabel("Giá (VNĐ)"));f.add(v);f.add(new JLabel("Nhóm món"));f.add(cat);
        if(JOptionPane.showConfirmDialog(this,f,row==null?"Thêm món":"Sửa món",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
            try(Connection c=DB.getConnection()){
                int cid;try(PreparedStatement q=c.prepareStatement("SELECT id FROM categories WHERE name=?")){q.setString(1,(String)cat.getSelectedItem());try(ResultSet rs=q.executeQuery()){rs.next();cid=rs.getInt(1);}}
                String sql=row==null?"INSERT INTO products(name,price,category_id,active) VALUES(?,?,?,1)":"UPDATE products SET name=?,price=?,category_id=? WHERE id=?";
                try(PreparedStatement q=c.prepareStatement(sql)){q.setString(1,n.getText().trim());q.setBigDecimal(2,new java.math.BigDecimal(v.getText().trim()));q.setInt(3,cid);if(row!=null)q.setInt(4,id);q.executeUpdate();}
                loadProducts("");
            }catch(Exception ex){error(ex);}
        }
    }
    private void toggleProduct(){int r=productTable.getSelectedRow();if(r<0)return;int id=(int)productTable.getValueAt(r,0);try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("UPDATE products SET active=1-active WHERE id=?")){p.setInt(1,id);p.executeUpdate();loadProducts("");}catch(Exception e){error(e);}}
    private JPanel tables(){
        JPanel p=page("SƠ ĐỒ BÀN / KHU VỰC","Xanh: trống • Đỏ: đang phục vụ • Vàng: đặt trước. Chọn bàn để đổi trạng thái hoặc chuyển bàn.");
        tableTable=table("Mã","Tên bàn","Khu vực","Trạng thái");
        JButton reload=button("Làm mới"),free=button("Đánh dấu trống"),busy=button("Đang phục vụ"),reserve=button("Đặt trước"),transfer=button("Chuyển bàn"),merge=button("Gộp bàn"),split=button("Tách bàn");
        reload.addActionListener(e->loadTables());free.addActionListener(e->setTableStatus("Trống"));busy.addActionListener(e->setTableStatus("Đang phục vụ"));reserve.addActionListener(e->setTableStatus("Đặt trước"));
        transfer.addActionListener(e->transferTable());merge.addActionListener(e->transferTable());split.addActionListener(e->JOptionPane.showMessageDialog(this,"Chức năng tách bàn sẽ tạo đơn riêng sau khi chọn hóa đơn đang mở."));
        p.add(actions(reload,free,busy,reserve,transfer,merge,split),BorderLayout.SOUTH);p.add(scroll(tableTable),BorderLayout.CENTER);return p;
    }
    private JPanel staff(){
        JPanel p=page("QUẢN LÝ NHÂN VIÊN","Lưu thông tin nhân viên, chức vụ, số điện thoại và trạng thái làm việc.");
        staffTable=table("Mã","Họ tên","Điện thoại","Chức vụ","Đang làm");
        JButton add=button("Thêm nhân viên"),edit=button("Sửa"),del=button("Xóa"),reload=button("Làm mới");
        add.addActionListener(e->staffDialog(null));edit.addActionListener(e->staffDialog(staffTable.getSelectedRow()));del.addActionListener(e->deleteSelected(staffTable,"employees"));reload.addActionListener(e->loadStaff());
        p.add(actions(add,edit,del,reload),BorderLayout.SOUTH);p.add(scroll(staffTable),BorderLayout.CENTER);return p;
    }
    private void staffDialog(Integer row){
        JTextField name=new JTextField(),phone=new JTextField(),role=new JTextField("Phục vụ");
        int id=-1;if(row!=null){int r=staffTable.convertRowIndexToModel(row);id=(int)staffTable.getValueAt(r,0);name.setText(""+staffTable.getValueAt(r,1));phone.setText(""+staffTable.getValueAt(r,2));role.setText(""+staffTable.getValueAt(r,3));}
        JPanel f=new JPanel(new GridLayout(0,1));f.add(new JLabel("Họ tên"));f.add(name);f.add(new JLabel("Điện thoại"));f.add(phone);f.add(new JLabel("Chức vụ"));f.add(role);
        if(JOptionPane.showConfirmDialog(this,f,"Thông tin nhân viên",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)try(Connection c=DB.getConnection()){
            String sql=row==null?"INSERT INTO employees(full_name,phone,position_name) VALUES(?,?,?)":"UPDATE employees SET full_name=?,phone=?,position_name=? WHERE id=?";
            try(PreparedStatement q=c.prepareStatement(sql)){q.setString(1,name.getText());q.setString(2,phone.getText());q.setString(3,role.getText());if(row!=null)q.setInt(4,id);q.executeUpdate();}loadStaff();
        }catch(Exception ex){error(ex);}
    }
    private JPanel shifts(){
        JPanel p=page("PHÂN CA LÀM VIỆC","Gán nhân viên vào ca theo ngày; ghi nhận ca sáng, chiều hoặc tối.");
        shiftTable=table("Mã ca","Nhân viên","Ngày","Ca","Bắt đầu","Kết thúc","Ghi chú");
        JButton add=button("Phân ca mới"),del=button("Xóa ca"),reload=button("Làm mới");
        add.addActionListener(e->shiftDialog());del.addActionListener(e->deleteSelected(shiftTable,"work_shifts"));reload.addActionListener(e->loadShifts());
        p.add(actions(add,del,reload),BorderLayout.SOUTH);p.add(scroll(shiftTable),BorderLayout.CENTER);return p;
    }
    private void shiftDialog(){
        JComboBox<String> emp=new JComboBox<>();java.util.Map<String,Integer> ids=new java.util.LinkedHashMap<>();
        try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,full_name FROM employees WHERE active=1")){while(r.next()){ids.put(r.getString(2),r.getInt(1));emp.addItem(r.getString(2));}}catch(Exception e){error(e);}
        JTextField date=new JTextField(java.time.LocalDate.now().toString()),start=new JTextField("07:00"),end=new JTextField("15:00"),note=new JTextField();
        JComboBox<String> type=new JComboBox<>(new String[]{"Ca sáng","Ca chiều","Ca tối"});
        JPanel f=new JPanel(new GridLayout(0,1));f.add(new JLabel("Nhân viên"));f.add(emp);f.add(new JLabel("Ngày (YYYY-MM-DD)"));f.add(date);f.add(new JLabel("Ca"));f.add(type);f.add(new JLabel("Giờ bắt đầu"));f.add(start);f.add(new JLabel("Giờ kết thúc"));f.add(end);f.add(new JLabel("Ghi chú"));f.add(note);
        if(JOptionPane.showConfirmDialog(this,f,"Phân ca",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)try(Connection c=DB.getConnection();PreparedStatement q=c.prepareStatement("INSERT INTO work_shifts(employee_id,shift_date,shift_name,start_time,end_time,note) VALUES(?,?,?,?,?,?)")){
            q.setInt(1,ids.get(emp.getSelectedItem()));q.setDate(2,Date.valueOf(date.getText()));q.setString(3,(String)type.getSelectedItem());q.setTime(4,Time.valueOf(start.getText()+":00"));q.setTime(5,Time.valueOf(end.getText()+":00"));q.setString(6,note.getText());q.executeUpdate();loadShifts();
        }catch(Exception ex){error(ex);}
    }
    private JPanel damages(){
        JPanel p=page("BỒI THƯỜNG TÀI SẢN","Ghi nhận ly, chén hoặc tài sản hư hỏng; số tiền được tổng hợp vào báo cáo.");
        damageTable=table("Mã","Mô tả","Số lượng","Đơn giá","Thành tiền","Ngày ghi nhận","Ghi chú");
        JButton add=button("Ghi nhận hư hỏng"),del=button("Xóa ghi nhận"),reload=button("Làm mới");
        add.addActionListener(e->damageDialog());del.addActionListener(e->deleteSelected(damageTable,"asset_compensations"));reload.addActionListener(e->loadDamages());
        p.add(actions(add,del,reload),BorderLayout.SOUTH);p.add(scroll(damageTable),BorderLayout.CENTER);return p;
    }
    private void damageDialog(){
        JTextField desc=new JTextField(),qty=new JTextField("1"),price=new JTextField("10000"),note=new JTextField();
        JPanel f=new JPanel(new GridLayout(0,1));f.add(new JLabel("Tài sản hư hỏng"));f.add(desc);f.add(new JLabel("Số lượng"));f.add(qty);f.add(new JLabel("Đơn giá"));f.add(price);f.add(new JLabel("Ghi chú"));f.add(note);
        if(JOptionPane.showConfirmDialog(this,f,"Bồi thường tài sản",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)try(Connection c=DB.getConnection();PreparedStatement q=c.prepareStatement("INSERT INTO asset_compensations(description,quantity,unit_cost,amount,note) VALUES(?,?,?,?,?)")){
            int n=Integer.parseInt(qty.getText());java.math.BigDecimal v=new java.math.BigDecimal(price.getText());q.setString(1,desc.getText());q.setInt(2,n);q.setBigDecimal(3,v);q.setBigDecimal(4,v.multiply(java.math.BigDecimal.valueOf(n)));q.setString(5,note.getText());q.executeUpdate();loadDamages();
        }catch(Exception ex){error(ex);}
    }
    private JPanel history(){
        JPanel p=page("HÓA ĐƠN / LỊCH SỬ","Tra cứu hóa đơn đã thanh toán theo thời gian và tổng tiền.");
        historyTable=table("Mã","Mã hóa đơn","Bàn","Thời gian","Tổng tiền","Trạng thái");
        JButton reload=button("Làm mới"),detail=button("Xem / In hóa đơn");
        reload.addActionListener(e->loadHistory());detail.addActionListener(e->showOrderDetail());
        p.add(actions(reload,detail),BorderLayout.SOUTH);p.add(scroll(historyTable),BorderLayout.CENTER);return p;
    }
    private JPanel reports(){
        JPanel p=page("BÁO CÁO DOANH THU","Tổng hợp doanh thu theo ngày, món bán chạy và khoản bồi thường tài sản.");
        JTextField from=new JTextField(java.time.LocalDate.now().withDayOfMonth(1).toString(),10),to=new JTextField(java.time.LocalDate.now().toString(),10);
        JTextArea result=new JTextArea();result.setEditable(false);result.setBackground(PANEL);result.setForeground(TEXT);result.setFont(new Font("Monospaced",Font.PLAIN,14));
        JButton run=button("Lập báo cáo");run.addActionListener(e->report(from.getText(),to.getText(),result));
        p.add(actions(new JLabel("Từ ngày"),from,new JLabel("Đến ngày"),to,run),BorderLayout.NORTH);p.add(new JScrollPane(result),BorderLayout.CENTER);return p;
    }
    private void report(String a,String b,JTextArea out){
        try(Connection c=DB.getConnection()){
            StringBuilder x=new StringBuilder("BÁO CÁO TỪ "+a+" ĐẾN "+b+"\n"+"=".repeat(58)+"\n");
            try(PreparedStatement q=c.prepareStatement("SELECT COUNT(*),COALESCE(SUM(total),0) FROM orders WHERE DATE(created_at) BETWEEN ? AND ?")){q.setDate(1,Date.valueOf(a));q.setDate(2,Date.valueOf(b));try(ResultSet r=q.executeQuery()){r.next();x.append("Số hóa đơn: ").append(r.getInt(1)).append("\nDoanh thu: ").append(money.format(r.getBigDecimal(2))).append(" VNĐ\n");}}
            try(PreparedStatement q=c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM asset_compensations WHERE DATE(created_at) BETWEEN ? AND ?")){q.setDate(1,Date.valueOf(a));q.setDate(2,Date.valueOf(b));try(ResultSet r=q.executeQuery()){r.next();x.append("Bồi thường tài sản: ").append(money.format(r.getBigDecimal(1))).append(" VNĐ\n\nMÓN BÁN CHẠY\n");}}
            try(PreparedStatement q=c.prepareStatement("SELECT p.name,SUM(d.quantity) sl FROM order_details d JOIN products p ON p.id=d.product_id JOIN orders o ON o.id=d.order_id WHERE DATE(o.created_at) BETWEEN ? AND ? GROUP BY p.id,p.name ORDER BY sl DESC LIMIT 10")){q.setDate(1,Date.valueOf(a));q.setDate(2,Date.valueOf(b));try(ResultSet r=q.executeQuery()){while(r.next())x.append(String.format("%-38s %5d\n",r.getString(1),r.getInt(2)));}}
            out.setText(x.toString());
        }catch(Exception ex){error(ex);}
    }
    private void loadProducts(String term){if(productTable==null)return;DefaultTableModel m=(DefaultTableModel)productTable.getModel();m.setRowCount(0);try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("SELECT p.id,p.name,c.name,p.price,IF(p.active=1,'Còn','Hết') FROM products p JOIN categories c ON c.id=p.category_id WHERE p.name LIKE ? ORDER BY p.id")){p.setString(1,"%"+term+"%");try(ResultSet r=p.executeQuery()){while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getBigDecimal(4),r.getString(5)});}}catch(Exception e){error(e);}}
    private void loadTables(){if(tableTable==null)return;DefaultTableModel m=(DefaultTableModel)tableTable.getModel();m.setRowCount(0);try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,table_name,zone_name,status FROM cafe_tables ORDER BY id")){while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getString(4)});}catch(Exception e){error(e);} tableTable.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer(){public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean focus,int row,int col){Component x=super.getTableCellRendererComponent(t,v,sel,focus,row,col);String s=String.valueOf(v);if(!sel){if(s.equals("Trống"))x.setForeground(new Color(65,190,125));else if(s.equals("Đang phục vụ"))x.setForeground(new Color(235,92,82));else if(s.equals("Đặt trước"))x.setForeground(new Color(235,184,70));else x.setForeground(TEXT);}setFont(getFont().deriveFont(Font.BOLD));return x;}}); }
    private void setTableStatus(String status){int r=tableTable.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Chọn một bàn trước.");return;}int id=(int)tableTable.getValueAt(r,0);try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("UPDATE cafe_tables SET status=? WHERE id=?")){p.setString(1,status);p.setInt(2,id);p.executeUpdate();loadTables();}catch(Exception e){error(e);}}
    private void transferTable(){int r=tableTable.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Chọn bàn cần chuyển.");return;}String target=JOptionPane.showInputDialog(this,"Nhập tên bàn đích (VD: Bàn 02):");if(target==null||target.isBlank())return;String source=String.valueOf(tableTable.getValueAt(r,1));try(Connection c=DB.getConnection()){c.setAutoCommit(false);int targetId;try(PreparedStatement q=c.prepareStatement("SELECT id FROM cafe_tables WHERE table_name=?")){q.setString(1,target);try(ResultSet rs=q.executeQuery()){if(!rs.next())throw new SQLException("Không tìm thấy bàn đích");targetId=rs.getInt(1);}}try(PreparedStatement q=c.prepareStatement("UPDATE orders SET table_id=? WHERE table_id=? AND status='Đang phục vụ'")){q.setInt(1,targetId);q.setInt(2,(int)tableTable.getValueAt(r,0));q.executeUpdate();}c.commit();loadTables();}catch(Exception e){error(e);}}
    private void loadStaff(){if(staffTable==null)return;DefaultTableModel m=(DefaultTableModel)staffTable.getModel();m.setRowCount(0);try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,full_name,phone,position_name,IF(active=1,'Có','Nghỉ') FROM employees ORDER BY id")){while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)});}catch(Exception e){error(e);}}
    private void loadShifts(){if(shiftTable==null)return;DefaultTableModel m=(DefaultTableModel)shiftTable.getModel();m.setRowCount(0);try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT w.id,e.full_name,w.shift_date,w.shift_name,w.start_time,w.end_time,w.note FROM work_shifts w JOIN employees e ON e.id=w.employee_id ORDER BY w.shift_date DESC")){while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getDate(3),r.getString(4),r.getTime(5),r.getTime(6),r.getString(7)});}catch(Exception e){error(e);}}
    private void loadDamages(){if(damageTable==null)return;DefaultTableModel m=(DefaultTableModel)damageTable.getModel();m.setRowCount(0);try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,description,quantity,unit_cost,amount,created_at,note FROM asset_compensations ORDER BY id DESC")){while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getInt(3),r.getBigDecimal(4),r.getBigDecimal(5),r.getTimestamp(6),r.getString(7)});}catch(Exception e){error(e);}}
    private void loadHistory(){if(historyTable==null)return;DefaultTableModel m=(DefaultTableModel)historyTable.getModel();m.setRowCount(0);try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT o.id,o.order_code,COALESCE(t.table_name,'Mang đi'),o.created_at,o.total,o.status FROM orders o LEFT JOIN cafe_tables t ON t.id=o.table_id ORDER BY o.id DESC")){while(r.next())m.addRow(new Object[]{r.getLong(1),r.getString(2),r.getString(3),r.getTimestamp(4),r.getBigDecimal(5),r.getString(6)});}catch(Exception e){error(e);}}
    private void showOrderDetail(){
        int r=historyTable.getSelectedRow();
        if(r<0){JOptionPane.showMessageDialog(this,"Chọn một hóa đơn trước.");return;}
        long id=((Number)historyTable.getValueAt(r,0)).longValue();
        new InvoiceDialog(this,id).setVisible(true);
    }
    private void deleteSelected(JTable t,String table){int r=t.getSelectedRow();if(r<0)return;int id=((Number)t.getValueAt(r,0)).intValue();if(JOptionPane.showConfirmDialog(this,"Xóa bản ghi đã chọn?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM "+table+" WHERE id=?")){p.setInt(1,id);p.executeUpdate();refreshAll();}catch(Exception e){error(e);}}
    private void refreshAll(){loadProducts("");loadTables();loadStaff();loadShifts();loadDamages();loadHistory();refreshStats();}
    private void refreshStats(){try(Connection c=DB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT (SELECT COALESCE(SUM(total),0) FROM orders WHERE DATE(created_at)=CURDATE()),(SELECT COUNT(*) FROM orders WHERE DATE(created_at)=CURDATE()),(SELECT COUNT(*) FROM cafe_tables WHERE status='Đang phục vụ')")){if(r.next()){if(revenueValue!=null)revenueValue.setText(money.format(r.getBigDecimal(1))+" đ");if(ordersValue!=null)ordersValue.setText(""+r.getInt(2));if(tablesValue!=null)tablesValue.setText(""+r.getInt(3));}}catch(Exception ignored){}}
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Lỗi dữ liệu",JOptionPane.ERROR_MESSAGE);}
}
