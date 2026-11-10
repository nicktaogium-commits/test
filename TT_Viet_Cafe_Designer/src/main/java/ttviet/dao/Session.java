package ttviet.dao;

public final class Session {
    private static int userId;
    private static String username = "";
    private static String fullName = "";
    private static String role = "";

    private Session() {}

    public static void setUser(int id, String user, String name, String userRole) {
        userId = id;
        username = user == null ? "" : user;
        fullName = name == null ? username : name;
        role = userRole == null ? "" : userRole;
    }
    public static int getUserId() { return userId; }
    public static String getUsername() { return username; }
    public static String getFullName() { return fullName; }
    public static String getRole() { return role; }
    public static void clear() {
        userId = 0;
        username = "";
        fullName = "";
        role = "";
    }
}
