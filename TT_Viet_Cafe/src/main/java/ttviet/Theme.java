package ttviet;
import java.awt.*;
import javax.swing.*;
public final class Theme {
 public static final Color BG = new Color(22,22,22), PANEL = new Color(34,34,34), GOLD = new Color(190,145,75), TEXT = new Color(245,238,225);
 private Theme() {}
 public static void style(Window w) { w.setBackground(BG); }
 public static JButton button(String s) {
  JButton b=new JButton(s); b.setBackground(GOLD); b.setForeground(Color.BLACK);
  b.setFocusPainted(false); b.setFont(new Font("Segoe UI",Font.BOLD,13)); return b;
 }
 public static void table(JTable t) {
  t.setRowHeight(30); t.setBackground(PANEL); t.setForeground(TEXT);
  t.setGridColor(new Color(70,65,55)); t.getTableHeader().setBackground(GOLD);
  t.getTableHeader().setForeground(Color.BLACK); t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
 }
}