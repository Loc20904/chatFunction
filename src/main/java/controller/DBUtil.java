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
        String url = "jdbc:sqlserver://LAPTOP-0L8RQE0C:1433;databaseName=chat;encrypt=false;trustServerCertificate=true;loginTimeout=30;";
        String user = "loc";
        String password = "123";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(url, user, password);
    }
}
