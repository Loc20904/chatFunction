package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.*;


public class s_chatBox extends HttpServlet {

    private static final String API_URL = "https://router.huggingface.co/fireworks-ai/inference/v1/chat/completions";
    // Thay bằng API key thật của bạn, kèm "Bearer "
    private static final String API_KEY = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Đọc JSON request từ client
        BufferedReader reader = request.getReader();
        StringBuilder requestData = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestData.append(line);
        }

        JsonObject requestJson = JsonParser.parseString(requestData.toString()).getAsJsonObject();
        String userMessage = requestJson.has("message") ? requestJson.get("message").getAsString().trim() : "";

        if (userMessage.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Message cannot be empty\"}");
            return;
        }

//        // Lấy danh sách xe từ DB
//        List<Car> allCars = CarRep.getall();
//        StringBuilder carData = new StringBuilder();
//        for (Car car : allCars) {
//            carData.append("Car Name: ").append(car.getCarName())
//                    .append(", Type: ").append(car.getType())
//                    .append(", Brand: ").append(car.getBrand())
//                    .append(", Price: ").append(car.getPrice())
//                    .append(", Year: ").append(car.getYearOfManufacture())
//                    .append(", Stock: ").append(car.getStockQuantity())
//                    .append("\n");
//        }
//
//        if (carData.length() == 0) {
//            JsonObject emptyResp = new JsonObject();
//            emptyResp.addProperty("response", "Vui lòng hỏi câu liên quan đến danh sách xe.");
//            response.setContentType("application/json;charset=UTF-8");
//            response.getWriter().write(emptyResp.toString());
//            return;
//        }
//
//        // Tạo prompt kết hợp dữ liệu database và câu hỏi user
//        String prompt = "give me the shortest answer. Database Car:\n" + carData.toString() + "\nUser Question: " + userMessage+". Answer in Vietnamese";
        String prompt = userMessage;
        // Tạo payload JSON theo chuẩn Chat Completion
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "accounts/fireworks/models/llama-v3p1-8b-instruct");
        requestBody.addProperty("stream", false);

        JsonArray messages = new JsonArray();

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);

        messages.add(userMsg);
        requestBody.add("messages", messages);

        // Gửi request đến Huggingface
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int status = conn.getResponseCode();
        InputStream inputStream = (status < 400) ? conn.getInputStream() : conn.getErrorStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder responseStr = new StringBuilder();
        while ((line = br.readLine()) != null) {
            responseStr.append(line.trim());
        }
        br.close();

        System.out.println("Response from HF API: " + responseStr);

        String aiResponseText = "Vui lòng hỏi câu liên quan đến danh sách xe.";
        try {
            JsonObject jsonResponse = JsonParser.parseString(responseStr.toString()).getAsJsonObject();
            if (jsonResponse.has("choices")) {
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject messageObj = firstChoice.getAsJsonObject("message");
                    if (messageObj != null && messageObj.has("content")) {
                        aiResponseText = messageObj.get("content").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            aiResponseText = "Lỗi xử lý phản hồi từ AI.";
            e.printStackTrace();
        }

        // Gửi response về client
        JsonObject clientResponse = new JsonObject();
        clientResponse.addProperty("response", aiResponseText.replace("**", "<br>").replace("database", "Showroom"));

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(clientResponse.toString());
    }
}
