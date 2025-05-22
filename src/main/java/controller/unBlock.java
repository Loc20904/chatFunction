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

@WebServlet("/unblock")
public class unBlock extends HttpServlet {

    private final blockDAO blockDAO = new blockDAO(); // bạn cần có class BlockDAO

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String blockerStr = request.getParameter("blockerId");
            String blockedStr = request.getParameter("blockedId");

            if (blockerStr == null || blockedStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Missing parameters\"}");
                return;
            }

            int blockerId = Integer.parseInt(blockerStr);
            int blockedId = Integer.parseInt(blockedStr);

            boolean success = blockDAO.unblockUser(blockerId, blockedId);

            if (success) {
                out.write("{\"status\": \"success\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"status\": \"fail\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Internal server error\"}");
        } finally {
            out.flush();
        }
    }
}

