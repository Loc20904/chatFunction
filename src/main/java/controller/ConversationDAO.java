package controller;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO {

    public static int getOrCreateConversation(int user1Id, int user2Id) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT conversation_id FROM conversations WHERE " +
                         "(user_a_id = ? AND user_b_id = ?) OR (user_a_id = ? AND user_b_id = ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user1Id);
            ps.setInt(2, user2Id);
            ps.setInt(3, user2Id);
            ps.setInt(4, user1Id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("conversation_id");

            // Nếu chưa tồn tại thì tạo mới
            sql = "INSERT INTO conversations (user_a_id, user_b_id) VALUES (?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, user1Id);
            ps.setInt(2, user2Id);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static List<Conversation> getConversationsByUser(int userId) {
        List<Conversation> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM conversations WHERE user_a_id = ? OR user_b_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Conversation(
                    rs.getInt("conversation_id"),
                    rs.getInt("user_a_id"),
                    rs.getInt("user_b_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int getOtherParticipant(int conversationId, int currentUserId) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT user_a_id, user_b_id FROM conversations WHERE conversation_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, conversationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userA = rs.getInt("user_a_id");
                int userB = rs.getInt("user_b_id");
                if (userA == currentUserId) return userB;
                if (userB == currentUserId) return userA;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Không tìm thấy
    }

    public static void main(String[] args) {
        System.out.println(getOrCreateConversation(1, 2));
        System.out.println(getOtherParticipant(1, 1)); // test thử
    }
}
