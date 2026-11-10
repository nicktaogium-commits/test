package ttviet.view;

import ttviet.dao.DB;
import ttviet.dao.OrderDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/** Man hinh phuc vu: so do ban, menu co anh va gio mon trong cung mot muc. */
public class ServicePanel extends JPanel {
    private static final Color BG=new Color(242,248,252), PANEL=Color.WHITE;
    private static final Color GOLD=new Color(30,112,166), TEXT=new Color(38,55,68);
    private final CardLayout workspaceLayout=new CardLayout();
    private final JPanel workspace=new JPanel(workspaceLayout);
    private final JPanel tableGrid=new JPanel(new GridLayout(0,3,8,8));
    private final JPanel menuGrid=new JPanel(new GridLayout(0,3,9,9));
    private final JTextField search=new JTextField();
    private final DefaultTableModel cart=new DefaultTableModel(new String[]{"Món","Đơn giá","Số lượng","Thành tiền","Ghi chú"},0) {
        @Override public boolean isCellEditable(int r,int c){return c==4;}
    };
    private final JTable cartTable=new JTable(cart);
    private final JLabel selectedLabel=new JLabel("Chưa chọn bàn");
    private final JLabel openOrderLabel=new JLabel("Chưa có đơn đang mở");
    private final JLabel totalLabel=new JLabel("Tổng món mới: 0 đ");
    private final NumberFormat money=NumberFormat.getNumberInstance(new Locale("vi","VN"));
    private final Map<Integer,String> tableNames=new LinkedHashMap<>();
    private final Map<Integer,List<Object[]>> draftCarts=new HashMap<>();
    private final List<Product> allProducts=new ArrayList<>();
    private Integer selectedTableId;

    public ServicePanel() {
        setLayout(new BorderLayout(10,10)); setBackground(BG);
        setBorder(new EmptyBorder(14,16,14,16));
        JLabel heading=new JLabel("PHỤC VỤ • QUẢN LÝ BÀN & GỌI MÓN");
        heading.setForeground(GOLD); heading.setFont(new Font("SansSerif",Font.BOLD,22));
        JLabel legend=new JLabel("● Trống     ● Đang phục vụ     ● Đặt trước     |     Chọn bàn rồi chọn món");
        legend.setForeground(TEXT);
        JPanel header=new JPanel(new BorderLayout(8,8)); header.setOpaque(false);
        header.add(heading,BorderLayout.NORTH); header.add(legend,BorderLayout.SOUTH);
        JPanel navigation=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4)); navigation.setOpaque(false);
        JButton showTables=button("SƠ ĐỒ BÀN"), showMenu=button("THỰC ĐƠN / GỌI MÓN");
        showTables.addActionListener(e->workspaceLayout.show(workspace,"TABLES"));
        showMenu.addActionListener(e->{if(selectedTableId==null)JOptionPane.showMessageDialog(this,"Vui lòng chọn bàn trước.");else workspaceLayout.show(workspace,"ORDER");});
        navigation.add(showTables); navigation.add(showMenu); header.add(navigation,BorderLayout.CENTER);
        add(header,BorderLayout.NORTH);

        tableGrid.setBackground(BG);
        JScrollPane tableScroll=new JScrollPane(tableGrid);
        tableScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(190,218,234)), "SƠ ĐỒ BÀN"));
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

        cartTable.setRowHeight(28);
        cartTable.setBackground(Color.WHITE); cartTable.setForeground(TEXT);
        cartTable.getTableHeader().setBackground(new Color(225,241,250));
        cartTable.getTableHeader().setForeground(new Color(25,91,135));
        cartTable.setFillsViewportHeight(true);
        JScrollPane cartScroll=new JScrollPane(cartTable);
        cartScroll.setPreferredSize(new Dimension(500,150));
        JPanel cartArea=new JPanel(new BorderLayout(6,5)); cartArea.setOpaque(false);
        JLabel cartTitle=new JLabel("MÓN ĐANG CHỌN");
        cartTitle.setForeground(GOLD); cartTitle.setFont(new Font("SansSerif",Font.BOLD,14));
        cartArea.add(cartTitle,BorderLayout.NORTH); cartArea.add(cartScroll,BorderLayout.CENTER);
        totalLabel.setForeground(GOLD); totalLabel.setFont(new Font("SansSerif",Font.BOLD,17));
        JButton remove=button("Xóa món"), save=button("LƯU / GỬI MÓN"), refresh=button("Làm mới"), transfer=button("CHUYỂN BÀN");
        remove.addActionListener(e->removeCartItem());
        save.addActionListener(e->saveToTable());
        refresh.addActionListener(e->{reloadTables();loadOpenOrderInfo();});
        transfer.addActionListener(e->transferSelectedTable());
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,2)); actions.setOpaque(false);
        actions.add(remove); actions.add(transfer); actions.add(refresh); actions.add(save);
        JPanel bottom=new JPanel(new BorderLayout()); bottom.setOpaque(false);
        bottom.add(totalLabel,BorderLayout.WEST); bottom.add(actions,BorderLayout.EAST);
        cartArea.add(bottom,BorderLayout.SOUTH);

        JPanel right=new JPanel(new BorderLayout(8,8)); right.setBackground(PANEL);
        right.setBorder(new EmptyBorder(10,10,10,10));
        right.add(info,BorderLayout.NORTH); right.add(menuArea,BorderLayout.CENTER); right.add(cartArea,BorderLayout.SOUTH);
        workspace.setBackground(BG);
        workspace.add(tableScroll,"TABLES"); workspace.add(right,"ORDER");
        add(workspace,BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){renderProducts();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){renderProducts();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){renderProducts();}
        });
        addComponentListener(new ComponentAdapter(){
            @Override public void componentShown(ComponentEvent e){reloadTables();loadOpenOrderInfo();}
        });
        loadProducts(); reloadTables();
    }

    private static class Product {
        final int id; final String name,imagePath; final BigDecimal price;
        Product(int id,String n,BigDecimal p,String img){this.id=id;name=n;price=p;imagePath=img;}
    }
    private JButton button(String text){
        JButton b=new JButton(text); b.setFocusPainted(false); b.setOpaque(true);
        b.setBackground(new Color(225,241,250)); b.setForeground(new Color(25,91,135));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(178,213,232)),new EmptyBorder(8,12,8,12)));
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
            JPanel card=new JPanel(new BorderLayout(10,4)); card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(215,230,239)),
                new EmptyBorder(7,7,7,7)));
            JLabel picture=new JLabel("🍽",SwingConstants.CENTER);
            picture.setForeground(GOLD); picture.setFont(new Font("SansSerif",Font.PLAIN,38));
            picture.setPreferredSize(new Dimension(130,72));
            ImageIcon icon=loadProductIcon(p.imagePath,124,72,p.name);
            if(icon!=null){picture.setText("");picture.setIcon(icon);}
            JLabel name=new JLabel("<html><center>"+escape(p.name)+"</center></html>",SwingConstants.CENTER);
            name.setForeground(TEXT); name.setFont(new Font("SansSerif",Font.BOLD,11));
            JLabel price=new JLabel(money.format(p.price)+" đ",SwingConstants.CENTER);
            price.setForeground(GOLD); price.setFont(new Font("SansSerif",Font.BOLD,13));
            JButton add=button("+ THÊM");
            add.addActionListener(e->addProduct(p));
            JPanel details=new JPanel(new GridLayout(3,1,3,3)); details.setOpaque(false);
            name.setHorizontalAlignment(SwingConstants.LEFT); price.setHorizontalAlignment(SwingConstants.LEFT);
            details.add(name); details.add(price); details.add(add);
            picture.setPreferredSize(new Dimension(112,78));
            card.add(picture,BorderLayout.WEST); card.add(details,BorderLayout.CENTER);
            menuGrid.add(card);
        }
        if(menuGrid.getComponentCount()==0){
            JLabel empty=new JLabel("Không tìm thấy món phù hợp.",SwingConstants.CENTER);
            empty.setForeground(TEXT);menuGrid.add(empty);
        }
        menuGrid.revalidate();menuGrid.repaint();
    }
    private ImageIcon loadProductIcon(String path,int w,int h,String productName){
        if(path!=null&&!path.isBlank()){
            File f=new File(path);
            if(!f.isAbsolute()&&!f.exists())f=new File("src/main/resources/"+path);
            if(!f.exists())f=new File("assets/products/"+path);
            if(f.exists()){
                ImageIcon source=new ImageIcon(f.getAbsolutePath());
                if(source.getIconWidth()>0)return new ImageIcon(source.getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH));
            }
        }
        // Minh hoa mon an tai cho, khong can tai anh tu Internet.
        BufferedImage art=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=art.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(247,239,220));g.fillRoundRect(2,2,w-4,h-4,16,16);
        g.setColor(new Color(213,195,160));g.fillOval(w/2-38,h/2-24,76,48);
        g.setColor(Color.WHITE);g.fillOval(w/2-32,h/2-18,64,36);
        String n=productName.toLowerCase(new Locale("vi","VN"));
        if(n.contains("pizza")){
            g.setColor(new Color(198,139,65));g.fillOval(w/2-25,h/2-13,50,26);
            g.setColor(new Color(178,61,43));for(int i=0;i<5;i++)g.fillOval(w/2-17+(i%3)*14,h/2-8+(i/3)*12,7,7);
        }else if(n.contains("bánh mì")){
            g.setColor(new Color(184,119,48));g.fillRoundRect(w/2-30,h/2-10,60,20,18,18);
            g.setColor(new Color(83,135,66));g.drawLine(w/2-18,h/2,w/2+18,h/2);
            g.setColor(new Color(178,61,43));g.drawLine(w/2-14,h/2+5,w/2+14,h/2+5);
        }else if(n.contains("cơm")){
            g.setColor(new Color(250,250,240));g.fillRoundRect(w/2-24,h/2-14,48,28,12,12);
            g.setColor(new Color(210,151,71));g.fillOval(w/2-10,h/2-6,20,12);
        }else if(n.contains("cafe")||n.contains("trà")||n.contains("rau")||n.contains("dừa")||n.contains("cam")||n.contains("chanh")||n.contains("đá")){
            g.setColor(new Color(89,155,119));g.fillRoundRect(w/2-15,h/2-17,30,36,8,8);
            g.setColor(new Color(220,241,221));g.fillRoundRect(w/2-10,h/2-12,20,25,5,5);
            g.setColor(new Color(180,73,45));g.fillRect(w/2-9,h/2+3,18,9);
            g.setColor(new Color(90,65,40));g.drawLine(w/2+4,h/2-17,w/2+14,h/2-28);
        }else{
            g.setColor(new Color(198,139,65));g.fillRoundRect(w/2-20,h/2-15,40,30,10,10);
            g.setColor(new Color(178,61,43));g.fillOval(w/2-9,h/2-7,18,14);
        }
        g.dispose();
        return new ImageIcon(art);
    }
    private String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    private void reloadTables(){
        tableGrid.removeAll(); tableNames.clear();
        try(Connection c=DB.getConnection()){
            // Dong bo mau ban tu don chua thanh toan, tranh ban van do sau khi da thu tien.
            try(Statement sync=c.createStatement()){
                sync.executeUpdate("UPDATE cafe_tables t SET status=CASE " +
                    "WHEN EXISTS(SELECT 1 FROM orders o WHERE o.table_id=t.id AND (o.payment_status IS NULL OR o.payment_status<>'Đã thanh toán') AND o.status='Đang phục vụ') THEN 'Đang phục vụ' " +
                    "WHEN t.status='Đang phục vụ' THEN 'Trống' ELSE t.status END");
            }
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,table_name,status FROM cafe_tables ORDER BY id")){
                while(r.next()){
                    int id=r.getInt(1);String name=r.getString(2),status=r.getString(3);tableNames.put(id,name);
                    String displayStatus="Đang phục vụ".equals(status)?"Chờ thanh toán":status;
                    JButton b=new JButton("<html><center><b>"+escape(name)+"</b><br>"+escape(displayStatus)+"</center></html>");
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
        if(selectedTableId!=null && selectedTableId==id)return;
        if(selectedTableId!=null)saveDraftCart(selectedTableId);
        selectedTableId=id;
        cart.setRowCount(0);
        List<Object[]> draft=draftCarts.get(id);
        if(draft!=null)for(Object[] row:draft)cart.addRow(row);
        updateTotal();
        selectedLabel.setText("Đang chọn: "+tableNames.get(id));
        loadOpenOrderInfo();reloadTables();workspaceLayout.show(workspace,"ORDER");
    }
    private void saveDraftCart(int tableId){
        List<Object[]> rows=new ArrayList<>();
        for(int i=0;i<cart.getRowCount();i++)
            rows.add(new Object[]{cart.getValueAt(i,0),cart.getValueAt(i,1),cart.getValueAt(i,2),cart.getValueAt(i,3),cart.getValueAt(i,4)});
        if(rows.isEmpty())draftCarts.remove(tableId);else draftCarts.put(tableId,rows);
    }
    private void transferSelectedTable(){
        if(selectedTableId==null){JOptionPane.showMessageDialog(this,"Chọn bàn cần chuyển trước.");return;}
        if(cart.getRowCount()>0){JOptionPane.showMessageDialog(this,"Bạn còn món chưa gửi. Hãy lưu/gửi món trước khi chuyển bàn.");return;}
        JComboBox<String> target=new JComboBox<>();
        Map<String,Integer> ids=new LinkedHashMap<>();
        for(Map.Entry<Integer,String> entry:tableNames.entrySet()){
            if(!entry.getKey().equals(selectedTableId)){ids.put(entry.getValue(),entry.getKey());target.addItem(entry.getValue());}
        }
        if(target.getItemCount()==0)return;
        if(JOptionPane.showConfirmDialog(this,target,"Chuyển đơn đang chờ sang bàn:",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        Integer targetId=ids.get(target.getSelectedItem());
        if(targetId!=null && OrderDAO.transferOpenTable(selectedTableId,targetId)){
            selectedTableId=targetId;
            selectedLabel.setText("Đang chọn: "+tableNames.get(targetId));
            reloadTables();loadOpenOrderInfo();
            JOptionPane.showMessageDialog(this,"Đã chuyển đơn sang "+tableNames.get(targetId)+".");
        }
    }
    private void loadOpenOrderInfo(){
        if(selectedTableId==null)return;
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(
            "SELECT order_code,total FROM orders WHERE table_id=? AND status='Đang phục vụ' AND (payment_status IS NULL OR payment_status<>'Đã thanh toán') ORDER BY id DESC LIMIT 1")){
            p.setInt(1,selectedTableId);
            try(ResultSet r=p.executeQuery()){
                if(r.next())openOrderLabel.setText("CHỜ THANH TOÁN • "+r.getString(1)+" • Đã gọi: "+money.format(r.getBigDecimal(2))+" đ");
                else openOrderLabel.setText("Bàn chưa có đơn đang mở — món mới sẽ tạo đơn đầu tiên.");
            }
        }catch(Exception e){showError(e);}
    }
    private void addProduct(Product p){
        if(selectedTableId==null){JOptionPane.showMessageDialog(this,"Vui lòng chọn bàn trên sơ đồ trước.");return;}
        JTextField quantity=new JTextField("1"); JTextField note=new JTextField();
        JPanel form=new JPanel(new GridLayout(0,1,5,5));
        form.add(new JLabel("Số lượng "+p.name)); form.add(quantity);
        form.add(new JLabel("Ghi chú cho bếp / phục vụ (không bắt buộc)")); form.add(note);
        if(JOptionPane.showConfirmDialog(this,form,"Thêm món",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{
            int qty=Integer.parseInt(quantity.getText().trim());if(qty<1||qty>99)throw new NumberFormatException();
            String itemNote=note.getText().trim(); int row=-1;
            for(int i=0;i<cart.getRowCount();i++)if(p.name.equals(cart.getValueAt(i,0)) && itemNote.equals(String.valueOf(cart.getValueAt(i,4)))){row=i;break;}
            if(row>=0){int next=Integer.parseInt(cart.getValueAt(row,2).toString())+qty;
                cart.setValueAt(next,row,2);cart.setValueAt(p.price.multiply(BigDecimal.valueOf(next)),row,3);
            }else cart.addRow(new Object[]{p.name,p.price,qty,p.price.multiply(BigDecimal.valueOf(qty)),itemNote});
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
            draftCarts.remove(selectedTableId);
            JOptionPane.showMessageDialog(this,"Đã lưu món thành công. Bàn đang ở trạng thái chờ thanh toán. Bạn có thể chuyển sang bàn khác hoặc tiếp tục gọi thêm món.");
            reloadTables();loadOpenOrderInfo();
        }
    }
    private void showError(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Lỗi dữ liệu",JOptionPane.ERROR_MESSAGE);}
}
