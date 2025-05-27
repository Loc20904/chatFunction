/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Thanh Loc
 */
public class UserDAO {

    public static User getUserByUsername(String username) {
        try (Connection conn = DBUtil.getConnect()) {
            String sql = "SELECT UserId, Username, PasswordHash FROM Users WHERE Username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = (ResultSet) ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("UserId"));
                u.setUsername(rs.getString("Username"));
                u.setPasswordHash(rs.getString("PasswordHash"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserById(int userId) {
        try (Connection conn = DBUtil.getConnect()) {
            String sql = "SELECT UserId, Username, PasswordHash FROM Users WHERE UserId = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("UserId"));
                u.setUsername(rs.getString("Username"));
                u.setPasswordHash(rs.getString("PasswordHash"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getAllExcept(int userId) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DBUtil.getConnect()) {
            String sql = "SELECT * FROM Users WHERE UserId != ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("UserId"), rs.getString("username"), rs.getString("passwordHash")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void main(String[] args) {
        System.out.println(getUserById(1));
    }
}
