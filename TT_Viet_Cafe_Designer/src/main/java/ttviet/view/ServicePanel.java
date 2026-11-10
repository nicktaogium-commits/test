package ttviet.view;

import ttviet.dao.DB;
import ttviet.dao.OrderDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/** Gộp sơ đồ bàn và gọi món vào cùng một màn hình phục vụ. */
public class ServicePanel extends JPanel {
    private static final Color BG=new Color(24,25,27), PANEL=new Color(37,38,41);
    private static final Color GOLD=new Color(205,160,92), TEXT=new Color(245,239,226);
    private final JPanel tableGrid=new JPanel(new GridLayout(0,4,10,10));
    private final JComboBox<Product> products=new JComboBox<>();
    private final JSpinner quantity=new JSpinner(new SpinnerNumberModel(1,1,99,1));
    private final DefaultTableModel cart=new DefaultTableModel(new String[]{"Món","Đơn giá","Số lượng","Thành tiền"},0) {
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable cartTable=new JTable(cart);
    private final JLabel selectedLabel=new JLabel("Chưa chọn bàn");
    private final JLabel openOrderLabel=new JLabel("Chưa có đơn đang mở");
    private final JLabel totalLabel=new JLabel("Tổng món mới: 0 đ");
    private final NumberFormat money=NumberFormat.getNumberInstance(new Locale("vi","VN"));
    private final Map<Integer,String> tableNames=new LinkedHashMap<>();
    private Integer selectedTableId;

    public ServicePanel() {
        setLayout(new BorderLayout(12,12)); setBackground(BG);
        setBorder(new EmptyBorder(18,20,18,20));
        JLabel heading=new JLabel("PHỤC VỤ • SƠ ĐỒ BÀN & GỌI MÓN");
        heading.setForeground(GOLD); heading.setFont(new Font("SansSerif",Font.BOLD,23));
        JLabel legend=new JLabel("  ● Trống     ● Đang phục vụ     ● Đặt trước     |     Chọn bàn trước khi thêm món");
        legend.setForeground(TEXT);
        JPanel header=new JPanel(new BorderLayout()); header.setOpaque(false);
        header.add(heading,BorderLayout.NORTH); header.add(legend,BorderLayout.SOUTH);
        add(header,BorderLayout.NORTH);

        tableGrid.setBackground(BG);
        JScrollPane tableScroll=new JScrollPane(tableGrid);
        tableScroll.setBorder(BorderFactory.createTitledBorder("SƠ ĐỒ BÀN"));
        tableScroll.getViewport().setBackground(BG);
        tableScroll.setPreferredSize(new Dimension(350,300));

        JPanel orderPanel=new JPanel(new BorderLayout(8,8)); orderPanel.setBackground(PANEL);
        orderPanel.setBorder(new EmptyBorder(12,12,12,12));
        selectedLabel.setForeground(GOLD); selectedLabel.setFont(new Font("SansSerif",Font.BOLD,17));
        openOrderLabel.setForeground(TEXT);
        JPanel info=new JPanel(new GridLayout(2,1,2,2)); info.setOpaque(false);
        info.add(selectedLabel); info.add(openOrderLabel);
        JPanel addControls=new JPanel(new FlowLayout(FlowLayout.LEFT,8,5)); addControls.setOpaque(false);
        products.setPreferredSize(new Dimension(280,32));
        JButton add=new JButton("THÊM MÓN"); add.addActionListener(e->addToCart());
        addControls.add(new JLabel("Món:")); addControls.add(products);
        addControls.add(new JLabel("SL:")); addControls.add(quantity); addControls.add(add);
        cartTable.setRowHeight(29);
        JScrollPane cartScroll=new JScrollPane(cartTable);
        JPanel bottom=new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        totalLabel.setForeground(GOLD); totalLabel.setFont(new Font("SansSerif",Font.BOLD,18));
        JButton remove=new JButton("Xóa món"), save=new JButton("GỬI MÓN VÀO BÀN / IN PHIẾU"), refresh=new JButton("Làm mới bàn");
        remove.addActionListener(e->removeCartItem());
        save.addActionListener(e->saveToTable());
        refresh.addActionListener(e->reloadTables());
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        actions.add(remove); actions.add(refresh); actions.add(save);
        bottom.add(totalLabel,BorderLayout.WEST); bottom.add(actions,BorderLayout.EAST);
        JPanel cartArea=new JPanel(new BorderLayout(6,6));
        cartArea.setOpaque(false);
        cartArea.add(cartScroll,BorderLayout.CENTER);
        cartArea.add(bottom,BorderLayout.SOUTH);
        orderPanel.add(info,BorderLayout.NORTH);
        orderPanel.add(addControls,BorderLayout.CENTER);
        orderPanel.add(cartArea,BorderLayout.SOUTH);

        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,tableScroll,orderPanel);
        split.setResizeWeight(0.34); split.setBorder(null);
        add(split,BorderLayout.CENTER);
        loadProducts(); reloadTables();
    }

    private static class Product {
        final String name; final BigDecimal price;
        Product(String n,BigDecimal p){name=n;price=p;}
        @Override public String toString(){return name+" — "+NumberFormat.getNumberInstance(new Locale("vi","VN")).format(price)+" đ";}
    }
    private void loadProducts(){
        products.removeAllItems();
        try(Connection c=DB.getConnection(); Statement s=c.createStatement();
            ResultSet r=s.executeQuery("SELECT name,price FROM products WHERE active=1 ORDER BY name")){
            while(r.next()) products.addItem(new Product(r.getString(1),r.getBigDecimal(2)));
        }catch(Exception e){showError(e);}
    }
    private void reloadTables(){
        tableGrid.removeAll(); tableNames.clear();
        try(Connection c=DB.getConnection(); Statement s=c.createStatement();
            ResultSet r=s.executeQuery("SELECT id,table_name,status FROM cafe_tables ORDER BY id")){
            while(r.next()){
                int id=r.getInt(1); String name=r.getString(2),status=r.getString(3);
                tableNames.put(id,name);
                JButton b=new JButton("<html><center><b>"+name+"</b><br>"+status+"</center></html>");
                b.setOpaque(true); b.setFocusPainted(false); b.setPreferredSize(new Dimension(145,78));
                b.setFont(new Font("SansSerif",Font.BOLD,13)); b.setForeground(Color.WHITE);
                if("Trống".equals(status)) b.setBackground(new Color(35,125,82));
                else if("Đang phục vụ".equals(status)) b.setBackground(new Color(170,55,48));
                else if("Đặt trước".equals(status)) b.setBackground(new Color(170,125,35));
                else b.setBackground(new Color(75,75,82));
                b.setBorder(BorderFactory.createLineBorder( id==(selectedTableId==null?-1:selectedTableId)?GOLD:new Color(90,90,90),2));
                b.addActionListener(e->selectTable(id));
                tableGrid.add(b);
            }
        }catch(Exception e){showError(e);}
        tableGrid.revalidate(); tableGrid.repaint();
        if(selectedTableId!=null && !tableNames.containsKey(selectedTableId)){selectedTableId=null;selectedLabel.setText("Chưa chọn bàn");openOrderLabel.setText("Chưa có đơn đang mở");}
    }
    private void selectTable(int id){
        selectedTableId=id;
        selectedLabel.setText("Đang chọn: "+tableNames.get(id));
        loadOpenOrderInfo();
        reloadTables();
    }
    private void loadOpenOrderInfo(){
        if(selectedTableId==null)return;
        try(Connection c=DB.getConnection(); PreparedStatement p=c.prepareStatement(
            "SELECT order_code,total FROM orders WHERE table_id=? AND status='Đang phục vụ' AND payment_status='Chờ thanh toán' ORDER BY id DESC LIMIT 1")){
            p.setInt(1,selectedTableId);
            try(ResultSet r=p.executeQuery()){
                if(r.next()) openOrderLabel.setText("Đơn đang mở: "+r.getString(1)+" • Đã gọi: "+money.format(r.getBigDecimal(2))+" đ");
                else openOrderLabel.setText("Bàn chưa có đơn đang mở — món mới sẽ tạo đơn đầu tiên.");
            }
        }catch(Exception e){showError(e);}
    }
    private void addToCart(){
        if(selectedTableId==null){JOptionPane.showMessageDialog(this,"Vui lòng chọn bàn trên sơ đồ trước.");return;}
        Product p=(Product)products.getSelectedItem(); if(p==null)return;
        int qty=(Integer)quantity.getValue();
        for(int i=0;i<cart.getRowCount();i++){
            if(p.name.equals(cart.getValueAt(i,0))){
                int next=(Integer)cart.getValueAt(i,2)+qty;
                cart.setValueAt(next,i,2);cart.setValueAt(p.price.multiply(BigDecimal.valueOf(next)),i,3);
                updateTotal();return;
            }
        }
        cart.addRow(new Object[]{p.name,p.price,qty,p.price.multiply(BigDecimal.valueOf(qty))});
        updateTotal();
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
        int ok=JOptionPane.showConfirmDialog(this,"Thêm các món mới vào đơn của "+table+"?\n"+totalLabel.getText()+"\nBàn sẽ chuyển sang màu đỏ khi có đơn phục vụ.","Xác nhận gọi món",JOptionPane.YES_NO_OPTION);
        if(ok!=JOptionPane.YES_OPTION)return;
        long orderId=OrderDAO.addItemsToOpenTable(cart,selectedTableId);
        if(orderId>0){
            cart.setRowCount(0);updateTotal();reloadTables();loadOpenOrderInfo();
            new InvoiceDialog(SwingUtilities.getWindowAncestor(this),orderId).setVisible(true);
        }
    }
    private void showError(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Lỗi dữ liệu",JOptionPane.ERROR_MESSAGE);}
}
