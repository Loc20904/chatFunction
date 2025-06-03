package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Thanh Loc
 */
public class DBUtil {

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://chat.cfwc8w0ey4aa.ap-southeast-1.rds.amazonaws.com:1433;databaseName=ChatAppDB;encrypt=false;trustServerCertificate=true;loginTimeout=30;";
        String user = "admin";
        String password = "Tuan210604";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(url, user, password);
    }
}
