/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Thanh Loc
 */
@ServerEndpoint(value = "/chat", configurator = HttpSessionConfigurator.class)
public class chat {

    // Map userId -> session WebSocket
    private static final Map<Integer, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // Lấy userId từ HttpSession (phải cấu hình EndpointConfig để truy cập)
        HttpSession httpSession = (HttpSession) config.getUserProperties().get("httpSession");
        User user = (User) httpSession.getAttribute("user");
        if (user != null) {
            sessions.put(user.getUserId(), session);
            System.out.println("User connected: " + user.getUsername());
        }
    }

    @OnMessage
    public void onMessage(String messageJson, Session session) {
        try (JsonReader reader = Json.createReader(new StringReader(messageJson))) {
            JsonObject json = reader.readObject();
            String type = json.containsKey("type") ? json.getString("type") : "message";

            Integer fromUserId = null;
            String fromUsername=null;
            for (Map.Entry<Integer, Session> entry : sessions.entrySet()) {
                if (entry.getValue().equals(session)) {
                    fromUserId = entry.getKey();
                    fromUsername=UserDAO.getUserById(fromUserId).getUsername();
                    break;
                }
            }
            if (fromUserId == null) {
                return;
            }

            switch (type) {
                case "message": {
                    int toUserId = json.getInt("toUserId");
                    String content = json.getString("content");

                    // Kiểm tra chặn
                    if (blockDAO.isBlocked(toUserId, fromUserId) || blockDAO.isBlocked(fromUserId, toUserId)) {
                        JsonObject blockNotify = Json.createObjectBuilder()
                                .add("type", "block")
                                .add("message", "You cannot send messages because one of you has blocked the other.")
                                .build();
                        session.getAsyncRemote().sendText(blockNotify.toString());
                        return;
                    }

                    // Gửi và lưu tin nhắn
                    int conversationId = ConversationDAO.getOrCreateConversation(fromUserId, toUserId);
                    int messageId = MessageDAO.saveMessage(conversationId, fromUserId, content);
                    String timestamp = LocalDateTime.now().toString();

                    JsonObject sendMessage = Json.createObjectBuilder()
                            .add("type", "message")
                            .add("fromUserId", fromUserId)
                            .add("fromUsername", fromUsername)
                            .add("toUserId", toUserId)
                            .add("content", content)
                            .add("messageId", messageId)
                            .add("conversationId", conversationId)
                            .add("timestamp", timestamp)
                            .build();

                    Session toSession = sessions.get(toUserId);
                    if (toSession != null && toSession.isOpen()) {
                        toSession.getAsyncRemote().sendText(sendMessage.toString());
                    }
                    session.getAsyncRemote().sendText(sendMessage.toString());
                    break;
                }

                case "recall": {
                    int toUserId = json.getInt("toUserId");
                    int messageId = json.getInt("messageId");

                    // 1. Update database để đánh dấu tin nhắn này là đã thu hồi (optional nhưng nên có)
                    MessageDAO.recallMessage(messageId, fromUserId);

                    // 2. Tạo JSON thông báo recall
                    JsonObject recallNotify = Json.createObjectBuilder()
                            .add("type", "recall")
                            .add("messageId", messageId)
                            .add("fromUserId", fromUserId)
                            .add("toUserId", toUserId)
                            .add("timestamp", LocalDateTime.now().toString()) // hoặc timestamp gốc nếu có
                            .build();

                    // 3. Gửi đến cả 2 phía (người gửi và người nhận)
                    Session fromSession = sessions.get(fromUserId);
                    Session toSession = sessions.get(toUserId);

                    if (fromSession != null && fromSession.isOpen()) {
                        fromSession.getAsyncRemote().sendText(recallNotify.toString());
                    }
                    if (toSession != null && toSession.isOpen()) {
                        toSession.getAsyncRemote().sendText(recallNotify.toString());
                    }
                    break;
                }
                case "block": {
                    int blockedId = json.getInt("blockedId");
                    // Lưu trạng thái block vào DB
                    blockDAO.blockUser(fromUserId, blockedId);

                    // Tạo JSON thông báo block
                    JsonObject notifyBlocker = Json.createObjectBuilder()
                            .add("type", "block_status")
                            .add("blockedId", blockedId)
                            .add("status", "blocked_by_me")
                            .build();

                    JsonObject notifyBlocked = Json.createObjectBuilder()
                            .add("type", "block_status")
                            .add("blockerId", fromUserId)
                            .add("status", "blocked_me")
                            .build();

                    // Gửi thông báo đến blocker (người gửi lệnh block)
                    session.getAsyncRemote().sendText(notifyBlocker.toString());

                    // Gửi thông báo đến người bị block (nếu đang online)
                    Session blockedSession = sessions.get(blockedId);
                    if (blockedSession != null && blockedSession.isOpen()) {
                        blockedSession.getAsyncRemote().sendText(notifyBlocked.toString());
                    }
                    break;
                }
                
                // Xử lý unblock
                case "unblock": {
                    int blockedId = json.getInt("blockedId");

                    // Xóa trạng thái block trong DB
                    blockDAO.unblockUser(fromUserId, blockedId);

                    JsonObject notifyBlocker = Json.createObjectBuilder()
                            .add("type", "block_status")
                            .add("blockedId", blockedId)
                            .add("status", "unblocked_by_me")
                            .build();

                    JsonObject notifyBlocked = Json.createObjectBuilder()
                            .add("type", "block_status")
                            .add("blockerId", fromUserId)
                            .add("status", "unblocked_me")
                            .build();

                    session.getAsyncRemote().sendText(notifyBlocker.toString());

                    Session blockedSession = sessions.get(blockedId);
                    if (blockedSession != null && blockedSession.isOpen()) {
                        blockedSession.getAsyncRemote().sendText(notifyBlocked.toString());
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.values().remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}
