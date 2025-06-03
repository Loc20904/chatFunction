package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://chat.cfwc8w0ey4aa.ap-southeast-1.rds.amazonaws.com:3306/ChatAppDB?useSSL=false&serverTimezone=UTC";
        String user = "admin";
        String password = "Tuan210604";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(url, user, password);
    }
}
