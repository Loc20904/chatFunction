/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import static Repository.DatabaseInfo.DBURL;
import static Repository.DatabaseInfo.DRIVERNAME;
import static Repository.DatabaseInfo.PASSDB;
import static Repository.DatabaseInfo.USERDB;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Thanh Loc
 */
public class DBUtil {
    public static java.sql.Connection getConnect() {
        try {
            Class.forName(DRIVERNAME);
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading driver" + e);
        }
        try {
            java.sql.Connection con = DriverManager.getConnection(DBURL, USERDB, PASSDB);
            return con;
        } catch (SQLException e) {
            System.out.println("Error: " + e);
        }
        return null;
    }
}
