package ttviet;
import ttviet.Controller.LoginController;
import ttviet.Dao.UserDAO;
import ttviet.View.LoginView;
import javax.swing.SwingUtilities;
public class App {
 public static void main(String[] args) {
  SwingUtilities.invokeLater(() -> {
   LoginView view = new LoginView();
   new LoginController(view, new UserDAO());
   view.setVisible(true);
  });
 }
}