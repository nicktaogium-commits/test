package ttviet.view;

import ttviet.dao.DB;
import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

/** Hộp thoại xem trước và in hóa đơn đã lưu trong cơ sở dữ liệu. */
public class InvoiceDialog extends JDialog {
    private final JEditorPane preview = new JEditorPane();

    public InvoiceDialog(Window owner, long orderId) {
        super(owner, "Hóa đơn T & T Viet", ModalityType.APPLICATION_MODAL);
        setSize(560, 720);
        setMinimumSize(new Dimension(480, 600));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));
        preview.setContentType("text/html");
        preview.setEditable(false);
        preview.setBackground(Color.WHITE);
        add(new JScrollPane(preview), BorderLayout.CENTER);

        JButton pay = new JButton("THANH TOÁN");
        JButton print = new JButton("IN HÓA ĐƠN");
        JButton close = new JButton("ĐÓNG");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(pay);
        actions.add(print);
        actions.add(close);
        add(actions, BorderLayout.SOUTH);
        pay.addActionListener(e -> {
            PaymentDialog dialog = new PaymentDialog(this, orderId);
            dialog.setVisible(true);
            loadInvoice(orderId);
        });
        print.addActionListener(e -> printInvoice());
        close.addActionListener(e -> dispose());
        loadInvoice(orderId);
    }

    private void loadInvoice(long orderId) {
        NumberFormat money = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Arial;color:#222;padding:18px'>")
            .append("<div style='text-align:center'><h1 style='color:#9B7138;margin-bottom:2px'>T &amp; T VIET</h1>")
            .append("<b>CAFE • FAST FOOD</b><br>PHIẾU TẠM TÍNH / HÓA ĐƠN</div><hr>");
        try (Connection c = DB.getConnection();
             PreparedStatement q = c.prepareStatement(
                 "SELECT o.order_code,o.created_at,o.total,o.status," +
                 "o.payment_status,COALESCE(t.table_name,'Mang đi'),COALESCE(u.full_name,u.username,'') " +
                 "FROM orders o LEFT JOIN cafe_tables t ON t.id=o.table_id " +
                 "LEFT JOIN users u ON u.id=o.user_id WHERE o.id=?")) {
            q.setLong(1, orderId);
            try (ResultSet r = q.executeQuery()) {
                if (!r.next()) {
                    preview.setText("<html><body>Không tìm thấy hóa đơn.</body></html>");
                    return;
                }
                html.append("<p><b>Mã hóa đơn:</b> ").append(escape(r.getString(1))).append("<br>")
                    .append("<b>Ngày giờ:</b> ").append(escape(String.valueOf(r.getTimestamp(2)))).append("<br>")
                    .append("<b>Trạng thái đơn:</b> ").append(escape(r.getString(4))).append("<br>")
                    .append("<b>Thanh toán:</b> ").append(escape(r.getString(5))).append("<br>")
                    .append("<b>Bàn:</b> ").append(escape(r.getString(6))).append("<br>")
                    .append("<b>Nhân viên:</b> ").append(escape(r.getString(7))).append("</p>");
                BigDecimal total = r.getBigDecimal(3);
                html.append("<table width='100%' cellspacing='0' cellpadding='7' style='border-collapse:collapse'>")
                    .append("<tr style='background:#eee'><th align='left'>Món</th><th>SL</th>")
                    .append("<th align='right'>Đơn giá</th><th align='right'>Thành tiền</th></tr>");
                try (PreparedStatement d = c.prepareStatement(
                    "SELECT p.name,od.quantity,od.unit_price,od.line_total " +
                    "FROM order_details od JOIN products p ON p.id=od.product_id WHERE od.order_id=?")) {
                    d.setLong(1, orderId);
                    try (ResultSet x = d.executeQuery()) {
                        while (x.next()) {
                            html.append("<tr><td style='border-bottom:1px solid #ddd'>").append(escape(x.getString(1)))
                                .append("</td><td align='center'>").append(x.getInt(2))
                                .append("</td><td align='right'>").append(money.format(x.getBigDecimal(3)))
                                .append("</td><td align='right'>").append(money.format(x.getBigDecimal(4))).append("</td></tr>");
                        }
                    }
                }
                html.append("</table><h2 style='text-align:right;color:#9B7138'>TỔNG CỘNG: ")
                    .append(money.format(total)).append(" VNĐ</h2>");
            }
        } catch (SQLException ex) {
            html.append("<p style='color:red'>Không tải được hóa đơn: ").append(escape(ex.getMessage())).append("</p>");
        }
        html.append("<hr><p style='text-align:center'>Cảm ơn quý khách và hẹn gặp lại!</p></body></html>");
        preview.setText(html.toString());
        preview.setCaretPosition(0);
    }

    private void printInvoice() {
        try {
            boolean printed = preview.print();
            if (printed) JOptionPane.showMessageDialog(this, "Đã gửi hóa đơn đến máy in.");
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Không thể in hóa đơn: " + ex.getMessage(),
                "Lỗi in", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
