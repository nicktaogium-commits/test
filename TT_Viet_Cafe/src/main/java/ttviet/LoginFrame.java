package ttviet;

import java.sql.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        initComponents();
        getContentPane().setBackground(Theme.BG);
        lblTitle.setForeground(Theme.GOLD);
        lblTitle.setFont(new java.awt.Font("Serif", java.awt.Font.BOLD, 32));
        lblSubtitle.setForeground(Theme.TEXT);
        lblUser.setForeground(Theme.TEXT);
        lblPass.setForeground(Theme.TEXT);
        btnLogin.setBackground(Theme.GOLD);
        btnLogin.setForeground(java.awt.Color.BLACK);
        btnLogin.addActionListener(e -> login());
        txtPass.addActionListener(e -> login());
    }

    //GEN-BEGIN:initComponents
    @SuppressWarnings("unchecked")
    private void initComponents() {
        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        lblPass = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        txtPass = new javax.swing.JPasswordField();
        btnLogin = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Đăng nhập - T & T Viet");
        setResizable(false);
        getContentPane().setLayout(null);

        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("T & T VIET");
        getContentPane().add(lblTitle);
        lblTitle.setBounds(90, 28, 340, 48);

        lblSubtitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitle.setText("QUẢN LÝ QUÁN CÀ PHÊ – THỨC ĂN NHANH");
        getContentPane().add(lblSubtitle);
        lblSubtitle.setBounds(50, 78, 420, 25);

        lblUser.setText("Tên đăng nhập");
        getContentPane().add(lblUser);
        lblUser.setBounds(95, 130, 120, 25);

        lblPass.setText("Mật khẩu");
        getContentPane().add(lblPass);
        lblPass.setBounds(95, 177, 120, 25);

        txtUser.setText("admin");
        getContentPane().add(txtUser);
        txtUser.setBounds(220, 130, 205, 30);
        getContentPane().add(txtPass);
        txtPass.setBounds(220, 177, 205, 30);

        btnLogin.setText("ĐĂNG NHẬP");
        getContentPane().add(btnLogin);
        btnLogin.setBounds(165, 235, 190, 38);

        setSize(new java.awt.Dimension(520, 360));
        setLocationRelativeTo(null);
    }
    //GEN-END:initComponents

    //GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel lblPass;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPasswordField txtPass;
    private javax.swing.JTextField txtUser;
    //GEN-END:variables

    private void login() {
        String sql = "SELECT id,full_name,role FROM users WHERE username=? AND password=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, txtUser.getText().trim());
            p.setString(2, new String(txtPass.getPassword()));
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    new MainFrame(r.getInt("id"), r.getString("full_name"), r.getString("role")).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không kết nối được MySQL. Hãy import database/tt_viet.sql.\n" + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }
}
