package ttviet.Controller;
import ttviet.Dao.UserDAO;import ttviet.Model.User;import ttviet.View.*;import javax.swing.*;
public class LoginController {
 private LoginView view;private UserDAO dao;
 public LoginController(LoginView view,UserDAO dao){this.view=view;this.dao=dao;view.getBtnLogin().addActionListener(e->login());}
 private void login(){try{User u=dao.login(view.getTxtUsername().getText().trim(),new String(view.getTxtPassword().getPassword()));if(u==null){JOptionPane.showMessageDialog(view,"Sai tài khoản hoặc mật khẩu.");return;}MainView main=new MainView(u);main.setVisible(true);view.dispose();}catch(Exception e){JOptionPane.showMessageDialog(view,"Lỗi kết nối CSDL: "+e.getMessage());}}
}