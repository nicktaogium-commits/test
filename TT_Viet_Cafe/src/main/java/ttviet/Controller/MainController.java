package ttviet.Controller;
import ttviet.View.*;import ttviet.Dao.UserDAO;
public class MainController {
 private final MainView view;
 public MainController(MainView view){this.view=view;view.getBtnLogout().addActionListener(e->{view.dispose();LoginView login=new LoginView();new LoginController(login,new UserDAO());login.setVisible(true);});}
}