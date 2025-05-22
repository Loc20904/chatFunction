/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/checkBlock")
public class CheckBlock extends HttpServlet {

    private final blockDAO blockDAO = new blockDAO(); // Đảm bảo bạn có class này

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String user1Str = request.getParameter("user1"); // người đang xem
        String user2Str = request.getParameter("user2"); // người bên kia

        PrintWriter out = response.getWriter();

        try {
            if (user1Str == null || user2Str == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Missing parameters\"}");
                return;
            }

            int user1 = Integer.parseInt(user1Str);
            int user2 = Integer.parseInt(user2Str);

            boolean blockedByUser1 = blockDAO.isBlocked(user1, user2); // user1 chặn user2
            boolean blockedByUser2 = blockDAO.isBlocked(user2, user1); // user2 chặn user1

            String jsonResponse = String.format(
                "{\"blockedByMe\": %b, \"blockedMe\": %b}",
                blockedByUser1, blockedByUser2
            );

            out.write(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Internal server error\"}");
        } finally {
            out.flush();
        }
    }
}
