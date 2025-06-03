
package controller;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public static int saveMessage(int conversationId, int senderId, String message) {
        int messageId = -1;
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO Messages (ConversationId, SenderId, Content, SentAt,isRead,type) VALUES (?, ?, ?, GETDATE(),0,'text')";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, conversationId);
            ps.setInt(2, senderId);
            ps.setString(3, message);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                messageId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messageId;
    }
    public static int saveMessage(int conversationId, int senderId, String message,String type) {
        int messageId = -1;
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO Messages (ConversationId, SenderId, Content, SentAt,isRead,type) VALUES (?, ?, ?, GETDATE(),0,?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, conversationId);
            ps.setInt(2, senderId);
            ps.setString(3, message);
            ps.setString(4, type);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                messageId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messageId;
    }

    public static List<Integer> getUsersWithUnreadMessages(int myUserId) {
        List<Integer> unreadSenders = new ArrayList<>();
        String sql
                = "SELECT DISTINCT SenderId "
                + "FROM Messages "
                + "WHERE SenderId != ? AND isRead = 0 AND ConversationId IN ( "
                + "    SELECT ConversationId "
                + "    FROM Conversations "
                + "    WHERE UserAId = ? OR UserBId = ? "
                + ")";

        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, myUserId);
            ps.setInt(2, myUserId);
            ps.setInt(3, myUserId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                unreadSenders.add(rs.getInt("SenderId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return unreadSenders;
    }

    public static void markMessagesAsRead(int fromUserId, int toUserId) {
        String sql = "UPDATE Messages SET isRead = 1 WHERE SenderId = ? AND ConversationId IN ("
                + "SELECT ConversationId FROM Conversations WHERE "
                + "(UserAId = ? AND UserBId = ?) OR (UserAId = ? AND UserBId = ?))";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fromUserId); // Sender
            ps.setInt(2, fromUserId);
            ps.setInt(3, toUserId);
            ps.setInt(4, toUserId);
            ps.setInt(5, fromUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Message> getMessagesByConversationId(int conversationId) {
        List<Message> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM Messages WHERE ConversationId = ? ORDER BY SentAt ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, conversationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Message msg = new Message(
                        rs.getInt("MessageId"),
                        rs.getInt("ConversationId"),
                        rs.getInt("SenderId"),
                        rs.getString("Content"),
                        rs.getTimestamp("SentAt"),
                        rs.getBoolean("is_recall"),
                        rs.getBoolean("isRead"),
                        rs.getString("type")
                );
                list.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đánh dấu là đã thu hồi
    public static boolean recallMessage(int messageId, int userId) {
        String sql = "UPDATE Messages SET is_recall = 1 WHERE MessageId = ? AND SenderId = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy người nhận để thông báo thu hồi
    public static int getReceiverIdOfMessage(int messageId, int senderId) {
        String sql = "SELECT ConversationId FROM Messages WHERE MessageId = ? AND SenderId = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setInt(2, senderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int conversationId = rs.getInt("ConversationId");
                return ConversationDAO.getOtherParticipant(conversationId, senderId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // không tìm thấy
    }

    public static void main(String[] args) throws SQLException {
        
    }
}
