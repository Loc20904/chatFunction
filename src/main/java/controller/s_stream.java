package controller;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap; // Sử dụng ConcurrentHashMap để đồng bộ hóa
import java.util.Map; // Import Map

public class s_stream extends HttpServlet {

    // Map để lưu trữ các PrintWriter của các client đang kết nối
    // Key: userId (để xác định người dùng nào đang kết nối), Value: PrintWriter
    private static final Map<Integer, PrintWriter> connectedClients = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy UserID từ session (giả sử bạn đã có User object trong session)
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated.");
            return;
        }

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no"); // Quan trọng cho Nginx proxy (nếu có)

        PrintWriter writer = response.getWriter();
        connectedClients.put(currentUser.getUserId(), writer); // Thêm client mới vào danh sách

        System.out.println("New SSE client connected: UserID " + currentUser.getUserId() + ". Total clients: " + connectedClients.size());

        // Khi kết nối SSE được mở, gửi danh sách tin nhắn chưa đọc cho người dùng đó
        sendUnreadListToClient(currentUser.getUserId(), writer);

        // Giữ kết nối mở vô thời hạn
        // Vòng lặp này sẽ chặn luồng cho đến khi client ngắt kết nối
        // Trong một ứng dụng thực tế, bạn có thể dùng kỹ thuật non-blocking I/O
        try {
            while (!Thread.currentThread().isInterrupted() && !writer.checkError()) {
                // Kiểm tra xem client có còn kết nối không
                // writer.checkError() sẽ trả về true nếu có lỗi I/O (client ngắt kết nối)
                Thread.sleep(5000); // Ngủ một chút để không chiếm CPU quá nhiều
                writer.write(":\n\n"); // Gửi một comment để giữ kết nối sống (keep-alive)
                writer.flush();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Đảm bảo trạng thái ngắt được giữ
            System.out.println("SSE connection interrupted for UserID: " + currentUser.getUserId());
        } finally {
            connectedClients.remove(currentUser.getUserId()); // Khi client ngắt kết nối, xóa khỏi danh sách
            System.out.println("SSE client disconnected: UserID " + currentUser.getUserId() + ". Total clients: " + connectedClients.size());
        }
    }

    // Phương thức để gửi danh sách tin nhắn chưa đọc khi client kết nối
    private void sendUnreadListToClient(int userId, PrintWriter writer) {
        List<Integer> unreadSenders = MessageDAO.getUsersWithUnreadMessages(userId);
        jakarta.json.JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Integer senderId : unreadSenders) {
            arrayBuilder.add(senderId);
        }

        JsonObject unreadNotice = Json.createObjectBuilder()
                .add("type", "unread_list")
                .add("senders", arrayBuilder)
                .build();
        try {
            writer.write("event: unread_list\n"); // Định nghĩa tên sự kiện
            writer.write("data: " + unreadNotice.toString() + "\n\n");
            writer.flush();
        } catch (Exception e) {
            System.err.println("Error sending unread_list to client " + userId + ": " + e.getMessage());
        }
    }

    // Phương thức tĩnh để gửi tin nhắn/sự kiện tới một client cụ thể
    public static void sendEventToClient(int userId, String eventType, JsonObject eventData) {
        PrintWriter writer = connectedClients.get(userId);
        if (writer != null) {
            try {
                writer.write("event: " + eventType + "\n");
                writer.write("data: " + eventData.toString() + "\n\n");
                writer.flush();
                System.out.println("Sent event '" + eventType + "' to UserID " + userId + ": " + eventData.toString());
            } catch (Exception e) {
                System.err.println("Error sending event to UserID " + userId + ", likely disconnected: " + e.getMessage());
                connectedClients.remove(userId); // Xóa client đã ngắt kết nối
            }
        } else {
            System.out.println("Client not connected for UserID: " + userId + ". Event '" + eventType + "' not sent.");
        }
    }

    // Phương thức tĩnh để gửi tin nhắn/sự kiện tới tất cả các client đang kết nối
    public static void sendEventToAllClients(String eventType, JsonObject eventData) {
        // Tạo một bản sao danh sách để tránh ConcurrentModificationException khi duyệt và xóa
        Set<Integer> currentClients = new HashSet<>(connectedClients.keySet());
        for (Integer userId : currentClients) {
            sendEventToClient(userId, eventType, eventData);
        }
    }
}