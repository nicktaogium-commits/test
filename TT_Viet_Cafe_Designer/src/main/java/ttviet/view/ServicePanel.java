package ttviet.view;

import ttviet.dao.DB;
import ttviet.dao.OrderDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/** Man hinh phuc vu: so do ban, menu co anh va gio mon trong cung mot muc. */
public class ServicePanel extends JPanel {
    private static final Color BG=new Color(24,25,27), PANEL=new Color(37,38,41);
    private static final Color GOLD=new Color(205,160,92), TEXT=new Color(245,239,226);
    private final JPanel tableGrid=new JPanel(new GridLayout(0,3,8,8));
    private final JPanel menuGrid=new JPanel(new GridLayout(0,3,9,9));
    private final JTextField search=new JTextField();
    private final DefaultTableModel cart=new DefaultTableModel(new String[]{"Món","Đơn giá","Số lượng","Thành tiền"},0) {
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable cartTable=new JTable(cart);
    private final JLabel selectedLabel=new JLabel("Chưa chọn bàn");
    private final JLabel openOrderLabel=new JLabel("Chưa có đơn đang mở");
    private final JLabel totalLabel=new JLabel("Tổng món mới: 0 đ");
    private final NumberFormat money=NumberFormat.getNumberInstance(new Locale("vi","VN"));
    private final Map<Integer,String> tableNames=new LinkedHashMap<>();
    private final List<Product> allProducts=new ArrayList<>();
    private Integer selectedTableId;

    public ServicePanel() {
        setLayout(new BorderLayout(10,10)); setBackground(BG);
        setBorder(new EmptyBorder(14,16,14,16));
        JLabel heading=new JLabel("PHỤC VỤ • SƠ ĐỒ BÀN & GỌI MÓN");
        heading.setForeground(GOLD); heading.setFont(new Font("SansSerif",Font.BOLD,22));
        JLabel legend=new JLabel("● Trống     ● Đang phục vụ     ● Đặt trước     |     Chọn bàn rồi chọn món");
        legend.setForeground(TEXT);
        JPanel header=new JPanel(new BorderLayout(4,4)); header.setOpaque(false);
        header.add(heading,BorderLayout.NORTH); header.add(legend,BorderLayout.SOUTH);
        add(header,BorderLayout.NORTH);

        tableGrid.setBackground(BG);
        JScrollPane tableScroll=new JScrollPane(tableGrid);
        tableScroll.setBorder(BorderFactory.createTitledBorder("SƠ ĐỒ BÀN"));
        tableScroll.getViewport().setBackground(BG);
        tableScroll.setMinimumSize(new Dimension(240,180));
        tableScroll.setPreferredSize(new Dimension(330,500));

        JPanel orderPanel=new JPanel(new BorderLayout(8,8)); orderPanel.setBackground(PANEL);
        orderPanel.setBorder(new EmptyBorder(10,10,10,10));
        selectedLabel.setForeground(GOLD); selectedLabel.setFont(new Font("SansSerif",Font.BOLD,17));
        openOrderLabel.setForeground(TEXT);
        JPanel info=new JPanel(new GridLayout(2,1,2,3)); info.setOpaque(false);
        info.add(selectedLabel); info.add(openOrderLabel);

        JPanel menuHeader=new JPanel(new BorderLayout(8,4)); menuHeader.setOpaque(false);
        JLabel menuTitle=new JLabel("CHỌN MÓN • HÌNH ẢNH & GIÁ");
        menuTitle.setForeground(GOLD); menuTitle.setFont(new Font("SansSerif",Font.BOLD,15));
        search.setPreferredSize(new Dimension(190,30));
        search.setToolTipText("Nhập tên món để tìm");
        menuHeader.add(menuTitle,BorderLayout.WEST); menuHeader.add(search,BorderLayout.EAST);
        menuGrid.setBackground(PANEL);
        JScrollPane menuScroll=new JScrollPane(menuGrid);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.getViewport().setBackground(PANEL);

        JPanel menuArea=new JPanel(new BorderLayout(6,6)); menuArea.setOpaque(false);
        menuArea.add(menuHeader,BorderLayout.NORTH); menuArea.add(menuScroll,BorderLayout.CENTER);

        cartTable.setRowHeight(25);
        cartTable.setFillsViewportHeight(true);
        JScrollPane cartScroll=new JScrollPane(cartTable);
        cartScroll.setPreferredSize(new Dimension(500,150));
        JPanel cartArea=new JPanel(new BorderLayout(6,5)); cartArea.setOpaque(false);
        JLabel cartTitle=new JLabel("MÓN ĐANG CHỌN");
        cartTitle.setForeground(GOLD); cartTitle.setFont(new Font("SansSerif",Font.BOLD,14));
        cartArea.add(cartTitle,BorderLayout.NORTH); cartArea.add(cartScroll,BorderLayout.CENTER);
        totalLabel.setForeground(GOLD); totalLabel.setFont(new Font("SansSerif",Font.BOLD,17));
        JButton remove=button("Xóa món"), save=button("GỬI MÓN / IN PHIẾU"), refresh=button("Làm mới");
        remove.addActionListener(e->removeCartItem());
        save.addActionListener(e->saveToTable());
        refresh.addActionListener(e->{reloadTables();loadOpenOrderInfo();});
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,2)); actions.setOpaque(false);
        actions.add(remove); actions.add(refresh); actions.add(save);
        JPanel bottom=new JPanel(new BorderLayout()); bottom.setOpaque(false);
        bottom.add(totalLabel,BorderLayout.WEST); bottom.add(actions,BorderLayout.EAST);
        cartArea.add(bottom,BorderLayout.SOUTH);

        JPanel right=new JPanel(new BorderLayout(8,8)); right.setBackground(PANEL);
        right.add(info,BorderLayout.NORTH);
        right.add(menuArea,BorderLayout.CENTER);
        right.add(cartArea,BorderLayout.SOUTH);
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,tableScroll,right);
        split.setResizeWeight(0.30); split.setDividerLocation(330); split.setContinuousLayout(true);
        split.setOneTouchExpandable(true); split.setBorder(null);
        add(split,BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){renderProducts();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){renderProducts();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){renderProducts();}
        });
        loadProducts(); reloadTables();
    }

    private static class Product {
        final int id; final String name,imagePath; final BigDecimal price;
        Product(int id,String n,BigDecimal p,String img){this.id=id;name=n;price=p;imagePath=img;}
    }
    private JButton button(String text){
        JButton b=new JButton(text); b.setFocusPainted(false);
        b.setBackground(new Color(92,66,34)); b.setForeground(TEXT);
        b.setFont(new Font("SansSerif",Font.BOLD,11)); return b;
    }
    private void loadProducts(){
        allProducts.clear();
        try(Connection c=DB.getConnection(); Statement s=c.createStatement();
            ResultSet r=s.executeQuery("SELECT id,name,price,COALESCE(image_path,'') FROM products WHERE active=1 ORDER BY name")){
            while(r.next())allProducts.add(new Product(r.getInt(1),r.getString(2),r.getBigDecimal(3),r.getString(4)));
            renderProducts();
        }catch(Exception e){showError(e);}
    }
    private void renderProducts(){
        menuGrid.removeAll();
        String term=search.getText()==null?"":search.getText().trim().toLowerCase(new Locale("vi","VN"));
        for(Product p:allProducts){
            if(!p.name.toLowerCase(new Locale("vi","VN")).contains(term))continue;
            JPanel card=new JPanel(new BorderLayout(4,4)); card.setBackground(new Color(48,48,51));
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(83,70,52)),
                new EmptyBorder(5,5,5,5)));
            JLabel picture=new JLabel("🍽",SwingConstants.CENTER);
            picture.setForeground(GOLD); picture.setFont(new Font("SansSerif",Font.PLAIN,38));
            picture.setPreferredSize(new Dimension(130,72));
            ImageIcon icon=loadProductIcon(p.imagePath,124,72);
            if(icon!=null){picture.setText("");picture.setIcon(icon);}
            JLabel name=new JLabel("<html><center>"+escape(p.name)+"</center></html>",SwingConstants.CENTER);
            name.setForeground(TEXT); name.setFont(new Font("SansSerif",Font.BOLD,11));
            JLabel price=new JLabel(money.format(p.price)+" đ",SwingConstants.CENTER);
            price.setForeground(GOLD); price.setFont(new Font("SansSerif",Font.BOLD,13));
            JButton add=button("+ THÊM");
            add.addActionListener(e->addProduct(p));
            card.add(picture,BorderLayout.NORTH); card.add(name,BorderLayout.CENTER);
            JPanel foot=new JPanel(new GridLayout(2,1,2,2)); foot.setOpaque(false);
            foot.add(price); foot.add(add); card.add(foot,BorderLayout.SOUTH);
            menuGrid.add(card);
        }
        if(menuGrid.getComponentCount()==0){
            JLabel empty=new JLabel("Không tìm thấy món phù hợp.",SwingConstants.CENTER);
            empty.setForeground(TEXT);menuGrid.add(empty);
        }
        menuGrid.revalidate();menuGrid.repaint();
    }
    private ImageIcon loadProductIcon(String path,int w,int h){
        if(path==null||path.isBlank())return null;
        File f=new File(path);
        if(!f.isAbsolute()&&!f.exists())f=new File("src/main/resources/"+path);
        if(!f.exists())f=new File("assets/products/"+path);
        if(!f.exists())return null;
        ImageIcon source=new ImageIcon(f.getAbsolutePath());
        if(source.getIconWidth()<=0)return null;
        Image image=source.getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }
    private String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    private void reloadTables(){
        tableGrid.removeAll(); tableNames.clear();
        try(Connection c=DB.getConnection()){
            // Dong bo mau ban tu don chua thanh toan, tranh ban van do sau khi da thu tien.
            try(Statement sync=c.createStatement()){
                sync.executeUpdate("UPDATE cafe_tables t SET t.status=CASE " +
                    "WHEN EXISTS(SELECT 1 FROM orders o WHERE o.table_id=t.id AND o.payment_status='Chờ thanh toán' AND o.status='Đang phục vụ') THEN 'Đang phục vụ' " +
                    "WHEN t.status='Đang phục vụ' THEN 'Trống' ELSE t.status END");
            }
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,table_name,status FROM cafe_tables ORDER BY id")){
                while(r.next()){
                    int id=r.getInt(1);String name=r.getString(2),status=r.getString(3);tableNames.put(id,name);
                    JButton b=new JButton("<html><center><b>"+escape(name)+"</b><br>"+escape(status)+"</center></html>");
                    b.setOpaque(true);b.setFocusPainted(false);b.setPreferredSize(new Dimension(92,76));
                    b.setFont(new Font("SansSerif",Font.BOLD,12));b.setForeground(Color.WHITE);
                    if("Trống".equals(status))b.setBackground(new Color(35,125,82));
                    else if("Đang phục vụ".equals(status))b.setBackground(new Color(170,55,48));
                    else if("Đặt trước".equals(status))b.setBackground(new Color(170,125,35));
                    else b.setBackground(new Color(75,75,82));
                    b.setBorder(BorderFactory.createLineBorder(id==(selectedTableId==null?-1:selectedTableId)?GOLD:new Color(90,90,90),2));
                    b.addActionListener(e->selectTable(id));tableGrid.add(b);
                }
            }
        }catch(Exception e){showError(e);}
        tableGrid.revalidate();tableGrid.repaint();
        if(selectedTableId!=null&&!tableNames.containsKey(selectedTableId)){
            selectedTableId=null;selectedLabel.setText("Chưa chọn bàn");openOrderLabel.setText("Chưa có đơn đang mở");
        }
    }
    private void selectTable(int id){
        if(selectedTableId!=null&&!selectedTableId.equals(id)&&cart.getRowCount()>0){
            int answer=JOptionPane.showConfirmDialog(this,"Giỏ hiện tại có món chưa gửi. Xóa giỏ để chuyển sang bàn khác?","Đổi bàn",JOptionPane.YES_NO_OPTION);
            if(answer!=JOptionPane.YES_OPTION)return;
            cart.setRowCount(0);updateTotal();
        }
        selectedTableId=id;selectedLabel.setText("Đang chọn: "+tableNames.get(id));
        loadOpenOrderInfo();reloadTables();
    }
    private void loadOpenOrderInfo(){
        if(selectedTableId==null)return;
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(
            "SELECT order_code,total FROM orders WHERE table_id=? AND status='Đang phục vụ' AND payment_status='Chờ thanh toán' ORDER BY id DESC LIMIT 1")){
            p.setInt(1,selectedTableId);
            try(ResultSet r=p.executeQuery()){
                if(r.next())openOrderLabel.setText("Đơn đang mở: "+r.getString(1)+" • Đã gọi: "+money.format(r.getBigDecimal(2))+" đ");
                else openOrderLabel.setText("Bàn chưa có đơn đang mở — món mới sẽ tạo đơn đầu tiên.");
            }
        }catch(Exception e){showError(e);}
    }
    private void addProduct(Product p){
        if(selectedTableId==null){JOptionPane.showMessageDialog(this,"Vui lòng chọn bàn trên sơ đồ trước.");return;}
        String input=JOptionPane.showInputDialog(this,"Số lượng "+p.name+":","1");
        if(input==null)return;
        try{
            int qty=Integer.parseInt(input.trim());if(qty<1||qty>99)throw new NumberFormatException();
            int row=-1;
            for(int i=0;i<cart.getRowCount();i++)if(p.name.equals(cart.getValueAt(i,0))){row=i;break;}
            if(row>=0){int next=Integer.parseInt(cart.getValueAt(row,2).toString())+qty;
                cart.setValueAt(next,row,2);cart.setValueAt(p.price.multiply(BigDecimal.valueOf(next)),row,3);
            }else cart.addRow(new Object[]{p.name,p.price,qty,p.price.multiply(BigDecimal.valueOf(qty))});
            updateTotal();
        }catch(Exception ex){JOptionPane.showMessageDialog(this,"Số lượng phải từ 1 đến 99.");}
    }
    private void removeCartItem(){int row=cartTable.getSelectedRow();if(row>=0)cart.removeRow(cartTable.convertRowIndexToModel(row));updateTotal();}
    private void updateTotal(){
        BigDecimal sum=BigDecimal.ZERO;
        for(int i=0;i<cart.getRowCount();i++)sum=sum.add(new BigDecimal(cart.getValueAt(i,3).toString()));
        totalLabel.setText("Tổng món mới: "+money.format(sum)+" đ");
    }
    private void saveToTable(){
        if(selectedTableId==null){JOptionPane.showMessageDialog(this,"Chọn bàn trước khi gửi món.");return;}
        if(cart.getRowCount()==0){JOptionPane.showMessageDialog(this,"Chưa có món mới trong danh sách.");return;}
        String table=tableNames.get(selectedTableId);
        int ok=JOptionPane.showConfirmDialog(this,"Thêm món mới vào đơn của "+table+"?\n"+totalLabel.getText(),
            "Xác nhận gọi món",JOptionPane.YES_NO_OPTION);
        if(ok!=JOptionPane.YES_OPTION)return;
        long orderId=OrderDAO.addItemsToOpenTable(cart,selectedTableId);
        if(orderId>0){
            cart.setRowCount(0);updateTotal();reloadTables();loadOpenOrderInfo();
            new InvoiceDialog(SwingUtilities.getWindowAncestor(this),orderId).setVisible(true);
        }
    }
    private void showError(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Lỗi dữ liệu",JOptionPane.ERROR_MESSAGE);}
}
