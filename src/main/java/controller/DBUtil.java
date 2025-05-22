/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://database-1.cfwc8w0ey4aa.ap-southeast-1.rds.amazonaws.com:1433;databaseName=chat;encrypt=false;trustServerCertificate=true;loginTimeout=30;";
        String user = "sa";
        String password = "Tuan210604";
        try {
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
} catch (ClassNotFoundException e) {
    e.printStackTrace();
}

        return DriverManager.getConnection(url, user, password);
    }

}

