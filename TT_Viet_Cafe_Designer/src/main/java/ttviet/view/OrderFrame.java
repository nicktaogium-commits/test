package ttviet.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import ttviet.dao.DB;
import ttviet.dao.OrderDAO;

/** Man hinh tao don: chon ro ban phuc vu hoac Mang di. */
public class OrderFrame extends JFrame {
    private JComboBox<String> cboTable, cboProduct;
    private JSpinner spQty;
    private JTable tblCart;
    private JLabel lblTotal;
    private DefaultTableModel model;
    private final NumberFormat money = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public OrderFrame() {
        initComponents();
        loadTables();
        loadProducts();
    }

    private void initComponents() {
        setTitle("Gọi món - T & T Viet");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(28, 28, 28));
        setLayout(new BorderLayout(16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(18, 18, 18));
        top.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("GỌI MÓN / TẠO PHIẾU");
        title.setForeground(new Color(205, 160, 92));
        title.setFont(new Font("Arial", Font.BOLD, 25));
        top.add(title, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        controls.setBackground(new Color(37, 38, 41));
        controls.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(205,160,92)), "Thông tin gọi món",
            0, 0, new Font("Arial", Font.BOLD, 14), new Color(205,160,92)));
        cboTable = new JComboBox<>();
        cboTable.setPreferredSize(new Dimension(170, 34));
        cboProduct = new JComboBox<>();
        cboProduct.setPreferredSize(new Dimension(390, 34));
        spQty = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        JButton add = new JButton("THÊM MÓN");
        controls.add(new JLabel("Bàn:")); controls.add(cboTable);
        controls.add(new JLabel("Món:")); controls.add(cboProduct);
        controls.add(new JLabel("SL:")); controls.add(spQty); controls.add(add);

        model = new DefaultTableModel(new String[]{"Món", "Đơn giá", "Số lượng", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblCart = new JTable(model);
        tblCart.setRowHeight(32);
        tblCart.setFont(new Font("Arial", Font.PLAIN, 14));
        tblCart.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        JScrollPane scroll = new JScrollPane(tblCart);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        center.add(controls, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(18,18,18));
        bottom.setBorder(BorderFactory.createEmptyBorder(14,24,14,24));
        lblTotal = new JLabel("Tổng tạm tính: 0 đ");
        lblTotal.setForeground(Color.WHITE);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 21));
        bottom.add(lblTotal, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton remove = new JButton("XÓA MÓN");
        JButton save = new JButton("LƯU ĐƠN / IN PHIẾU");
        JButton close = new JButton("ĐÓNG");
        actions.add(remove); actions.add(save); actions.add(close);
        bottom.add(actions, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        add.addActionListener(e -> addItem());
        remove.addActionListener(e -> removeItem());
        save.addActionListener(e -> saveOrder());
        close.addActionListener(e -> dispose());
    }

    private void loadTables() {
        cboTable.removeAllItems();
        try (Connection c = DB.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT id,table_name FROM cafe_tables ORDER BY id")) {
            while (r.next()) cboTable.addItem(r.getInt(1) + " - " + r.getString(2));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách bàn: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try (Connection c = DB.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT name,price FROM products WHERE active=1 ORDER BY name")) {
            while (r.next()) cboProduct.addItem(r.getString(1) + " | " + r.getBigDecimal(2).toPlainString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải món: " + e.getMessage());
        }
    }

    private void addItem() {
        String value = (String) cboProduct.getSelectedItem();
        if (value == null) return;
        int split = value.lastIndexOf(" | ");
        if (split < 0) return;
        String name = value.substring(0, split);
        BigDecimal price = new BigDecimal(value.substring(split + 3));
        int quantity = (Integer) spQty.getValue();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (name.equals(model.getValueAt(i, 0))) {
                int newQty = (Integer) model.getValueAt(i, 2) + quantity;
                model.setValueAt(newQty, i, 2);
                model.setValueAt(price.multiply(BigDecimal.valueOf(newQty)), i, 3);
                updateTotal();
                return;
            }
        }
        model.addRow(new Object[]{name, price, quantity, price.multiply(BigDecimal.valueOf(quantity))});
        updateTotal();
    }

    private void removeItem() {
        int row = tblCart.getSelectedRow();
        if (row >= 0) model.removeRow(tblCart.convertRowIndexToModel(row));
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < model.getRowCount(); i++)
            total = total.add(new BigDecimal(model.getValueAt(i, 3).toString()));
        lblTotal.setText("Tổng tạm tính: " + money.format(total) + " đ");
    }

    private Integer selectedTableId() {
        String selected = (String) cboTable.getSelectedItem();
        if (selected == null || selected.endsWith(" - Mang đi")) return null;
        return Integer.valueOf(selected.substring(0, selected.indexOf(" - ")));
    }

    private void saveOrder() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Bạn chưa thêm món vào đơn.");
            return;
        }
        String table = String.valueOf(cboTable.getSelectedItem());
        int confirm = JOptionPane.showConfirmDialog(this,
            "Lưu đơn cho " + table + " với tổng tiền " + lblTotal.getText() +
            "?\nĐơn sẽ ở trạng thái CHỜ THANH TOÁN. In phiếu không có nghĩa là đã thu tiền.",
            "Xác nhận lưu đơn", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        long orderId = OrderDAO.saveAndGetId(model, selectedTableId());
        if (orderId > 0) {
            JOptionPane.showMessageDialog(this,
                "Đã lưu đơn ở trạng thái CHỜ THANH TOÁN. Thu ngân chỉ xác nhận đã thanh toán khi nhận được giao dịch hợp lệ.");
            new InvoiceDialog(this, orderId).setVisible(true);
            model.setRowCount(0);
            updateTotal();
        }
    }
}
