package ttviet.view;

import ttviet.dao.DB;
import ttviet.dao.PaymentDAO;
import ttviet.dao.Session;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

/** Chon thanh toan tien mat hoac chuyen khoan QR. Chuyen khoan chi duoc xac nhan boi webhook. */
public class PaymentDialog extends JDialog {
    private final long orderId;
    private final NumberFormat money=NumberFormat.getNumberInstance(new Locale("vi","VN"));
    private final JLabel status=new JLabel("Chưa thanh toán",SwingConstants.CENTER);
    private final JPanel methodPanel=new JPanel(new CardLayout());
    private final JTextField receivedField=new JTextField("0",12);
    private BigDecimal total=BigDecimal.ZERO;
    private String orderCode="";
    private boolean alreadyPaid=false;

    public PaymentDialog(Window owner,long orderId) {
        super(owner,"THANH TOÁN HÓA ĐƠN",ModalityType.APPLICATION_MODAL);
        this.orderId=orderId;
        String role=Session.getRole()==null?"":Session.getRole().trim().toLowerCase(Locale.ROOT);
        boolean allowed=role.equals("cashier")||role.equals("thu ngân")||role.equals("thu ngan")||
            role.equals("manager")||role.equals("admin")||role.equals("quản lý")||role.equals("quan ly");
        if(!allowed){
            JOptionPane.showMessageDialog(owner,"Chỉ thu ngân hoặc quản lý mới được mở chức năng thanh toán.","Không đủ quyền",JOptionPane.WARNING_MESSAGE);
            dispose(); return;
        }
        setSize(560,620);setMinimumSize(new Dimension(500,560));setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));
        getContentPane().setBackground(new Color(35,36,39));
        loadOrder();
        JLabel title=new JLabel("THANH TOÁN • "+orderCode,SwingConstants.CENTER);
        title.setForeground(new Color(205,160,92));title.setFont(new Font("SansSerif",Font.BOLD,20));
        JLabel amount=new JLabel("TỔNG CỘNG: "+money.format(total)+" VNĐ",SwingConstants.CENTER);
        amount.setForeground(Color.WHITE);amount.setFont(new Font("SansSerif",Font.BOLD,18));
        JPanel header=new JPanel(new GridLayout(3,1,4,4));header.setOpaque(false);
        header.setBorder(new EmptyBorder(15,15,5,15));header.add(title);header.add(amount);
        status.setForeground(new Color(235,190,90));header.add(status);add(header,BorderLayout.NORTH);

        JPanel choose=new JPanel(new FlowLayout(FlowLayout.CENTER,12,8));choose.setOpaque(false);
        JRadioButton cash=new JRadioButton("TIỀN MẶT",true),bank=new JRadioButton("CHUYỂN KHOẢN QR");
        cash.setOpaque(false);bank.setOpaque(false);cash.setForeground(Color.WHITE);bank.setForeground(Color.WHITE);
        ButtonGroup group=new ButtonGroup();group.add(cash);group.add(bank);
        cash.setEnabled(!alreadyPaid);bank.setEnabled(!alreadyPaid);
        choose.add(cash);choose.add(bank);
        JPanel cashPanel=cashPanel(),bankPanel=bankPanel();
        methodPanel.add(cashPanel,"CASH");methodPanel.add(bankPanel,"BANK");
        JPanel center=new JPanel(new BorderLayout());center.setOpaque(false);
        center.add(choose,BorderLayout.NORTH);center.add(methodPanel,BorderLayout.CENTER);
        add(center,BorderLayout.CENTER);
        cash.addActionListener(e->showMethod("CASH"));
        bank.addActionListener(e->showMethod("BANK"));
        JButton close=new JButton("ĐÓNG");close.addActionListener(e->dispose());
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));bottom.add(close);add(bottom,BorderLayout.SOUTH);
        showMethod("CASH");
        if(isPaidNow())markAlreadyPaid();
    }

    private void loadOrder(){
        try(Connection c=DB.getConnection();PreparedStatement p=c.prepareStatement(
            "SELECT order_code,total,payment_status FROM orders WHERE id=?")){
            p.setLong(1,orderId);
            try(ResultSet r=p.executeQuery()){
                if(!r.next())throw new SQLException("Không tìm thấy hóa đơn.");
                orderCode=r.getString(1);total=r.getBigDecimal(2);
                if("Đã thanh toán".equals(r.getString(3))){alreadyPaid=true;status.setText("ĐÃ THANH TOÁN");}
            }
        }catch(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}
    }

    private JPanel cashPanel(){
        JPanel p=new JPanel(new GridBagLayout());p.setOpaque(false);
        GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(9,9,9,9);g.fill=GridBagConstraints.HORIZONTAL;
        JLabel due=new JLabel("Khách cần trả: "+money.format(total)+" đ");
        due.setForeground(Color.WHITE);due.setFont(new Font("SansSerif",Font.BOLD,17));
        g.gridx=0;g.gridy=0;g.gridwidth=2;p.add(due,g);
        JLabel label=new JLabel("Tiền khách đưa (VNĐ):");label.setForeground(Color.WHITE);
        g.gridy=1;g.gridwidth=1;p.add(label,g);g.gridx=1;p.add(receivedField,g);
        JLabel change=new JLabel("Tiền thừa: 0 đ");change.setForeground(new Color(205,160,92));
        g.gridx=0;g.gridy=2;g.gridwidth=2;p.add(change,g);
        receivedField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            private void update(){try{BigDecimal v=new BigDecimal(receivedField.getText().trim().replace(",","").replace(".",""));change.setText("Tiền thừa: "+money.format(v.subtract(total).max(BigDecimal.ZERO))+" đ");}catch(Exception ex){change.setText("Tiền thừa: —");}}
            public void insertUpdate(javax.swing.event.DocumentEvent e){update();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){update();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){update();}
        });
        JButton confirm=new JButton("XÁC NHẬN ĐÃ THU TIỀN MẶT");
        confirm.setEnabled(!alreadyPaid);
        confirm.addActionListener(e->collectCash());
        g.gridy=3;p.add(confirm,g);
        JLabel note=new JLabel("<html><center>Chỉ xác nhận sau khi đã nhận đủ tiền từ khách.</center></html>");
        note.setForeground(new Color(220,205,177));g.gridy=4;p.add(note,g);
        return p;
    }

    private JPanel bankPanel(){
        JPanel p=new JPanel(new BorderLayout(8,8));p.setOpaque(false);
        String bankCode=System.getenv("TTV_BANK_CODE");
        String account=System.getenv("TTV_BANK_ACCOUNT");
        String accountName=System.getenv("TTV_BANK_ACCOUNT_NAME");
        JLabel qr=new JLabel("Cần cấu hình tài khoản ngân hàng để tạo QR.",SwingConstants.CENTER);
        qr.setForeground(Color.WHITE);qr.setPreferredSize(new Dimension(320,300));
        if(bankCode!=null&&!bankCode.isBlank()&&account!=null&&!account.isBlank()){
            try{
                String url="https://img.vietqr.io/image/"+encPath(bankCode)+"-"+encPath(account)+"-compact2.png?amount="+
                    total.toBigInteger().toString()+"&addInfo="+URLEncoder.encode(orderCode,StandardCharsets.UTF_8)+
                    (accountName==null||accountName.isBlank()?"":"&accountName="+URLEncoder.encode(accountName,StandardCharsets.UTF_8));
                ImageIcon icon=new ImageIcon(new java.net.URL(url));
                if(icon.getIconWidth()>0){qr.setText("");qr.setIcon(new ImageIcon(icon.getImage().getScaledInstance(280,280,Image.SCALE_SMOOTH)));}
            }catch(Exception ex){qr.setText("Không tải được QR. Kiểm tra kết nối Internet.");}
        }else{
            qr.setText("<html><center>Chưa cấu hình tài khoản nhận tiền.<br><br>"+
                "TTV_BANK_CODE<br>TTV_BANK_ACCOUNT<br>TTV_BANK_ACCOUNT_NAME</center></html>");
        }
        JLabel instruction=new JLabel("<html><center>Quét QR và chuyển đúng <b>"+money.format(total)+" VNĐ</b><br>"+
            "Nội dung chuyển khoản: <b>"+orderCode+"</b><br>"+
            "Hệ thống chỉ xác nhận khi SePay gửi webhook hợp lệ.</center></html>",SwingConstants.CENTER);
        instruction.setForeground(Color.WHITE);
        p.add(qr,BorderLayout.CENTER);p.add(instruction,BorderLayout.SOUTH);
        javax.swing.Timer timer=new javax.swing.Timer(3000,e->{
            try{if(PaymentDAO.isPaid(orderId)){status.setText("ĐÃ NHẬN CHUYỂN KHOẢN • ĐÃ THANH TOÁN");((javax.swing.Timer)e.getSource()).stop();}}
            catch(Exception ignored){}
        });
        timer.start();
        addWindowListener(new java.awt.event.WindowAdapter(){@Override public void windowClosed(java.awt.event.WindowEvent e){timer.stop();}});
        return p;
    }

    private String encPath(String s){return s.replaceAll("[^A-Za-z0-9_-]","");}
    private void showMethod(String key){((CardLayout)methodPanel.getLayout()).show(methodPanel,key);}
    private void collectCash(){
        try{
            String raw=receivedField.getText().trim().replace(",","").replace(".","");
            BigDecimal received=new BigDecimal(raw);
            if(received.compareTo(total)<0){JOptionPane.showMessageDialog(this,"Khách đưa chưa đủ tiền.");return;}
            BigDecimal change=received.subtract(total);
            if(JOptionPane.showConfirmDialog(this,"Đã nhận "+money.format(received)+" đ?\nTrả lại "+money.format(change)+" đ?","Xác nhận thu tiền",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            PaymentDAO.payCash(orderId,received);
            status.setText("ĐÃ THANH TOÁN • TIỀN MẶT");
            JOptionPane.showMessageDialog(this,"Đã ghi nhận thanh toán tiền mặt. Bàn sẽ được giải phóng nếu không còn đơn chưa thanh toán.");
            dispose();
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Không thể thanh toán",JOptionPane.ERROR_MESSAGE);}
    }
    private boolean isPaidNow(){try{return PaymentDAO.isPaid(orderId);}catch(Exception e){return false;}}
    private void markAlreadyPaid(){status.setText("HÓA ĐƠN ĐÃ THANH TOÁN");}
}
