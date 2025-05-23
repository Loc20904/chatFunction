package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static final String URL = "jdbc:mysql://database-2.cfwc8w0ey4aa.ap-southeast-1.rds.amazonaws.com/UsedGoodsDB?useSSL=false&serverTimezone=UTC";
    private static final String USER = "admin"; // đổi nếu cần
    private static final String PASSWORD = "123456789"; // nhập password nếu có

    public static Connection getConnection() {
        try {
            // Không bắt buộc gọi Class.forName nếu dùng Maven, nhưng có thể thêm để an toàn
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("⚠️ Không tìm thấy driver MySQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi kết nối đến cơ sở dữ liệu MySQL.");
            e.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args) {
        Connection conn = DBUtil.getConnection();
        if (conn != null) {
            System.out.println("✅ Kết nối MySQL thành công!");
        } else {
            System.out.println("❌ Không thể kết nối MySQL.");
        }
    }
}
