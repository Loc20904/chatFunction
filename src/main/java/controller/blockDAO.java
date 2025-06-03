
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class blockDAO {

    // Hàm lấy connection, bạn cần thay thông tin DB đúng với môi trường bạn
    private static Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    // Kiểm tra xem blockerUserId đã block blockedUserId chưa
    public static boolean isBlocked(int blockerUserId, int blockedUserId) {
        String sql = "SELECT 1 FROM BlockedUsers WHERE blockerUserId = ? AND blockedUserId = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blockerUserId);
            ps.setInt(2, blockedUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();  // Nếu có dòng trả về nghĩa là đã block
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // lỗi thì coi như chưa block
        }
    }

    // Thêm bản ghi block user
    public static boolean blockUser(int blockerUserId, int blockedUserId) {
        if (isBlocked(blockerUserId, blockedUserId)) {
            return false; // Đã block rồi, không thêm nữa
        }

        String sql = "INSERT INTO BlockedUsers (blockerUserId, blockedUserId) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blockerUserId);
            ps.setInt(2, blockedUserId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa bản ghi unblock user
    public static boolean unblockUser(int blockerUserId, int blockedUserId) {
        String sql = "DELETE FROM BlockedUsers WHERE blockerUserId = ? AND blockedUserId = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blockerUserId);
            ps.setInt(2, blockedUserId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
