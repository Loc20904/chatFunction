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

// Đảm bảo URL này trùng với URL bạn gọi từ fetch() bên client
@WebServlet("/block")
public class BlockServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String blockerId = req.getParameter("blockerId");
        String blockedId = req.getParameter("blockedId");

        System.out.println("BlockServlet: blockerId = " + blockerId + ", blockedId = " + blockedId);

        if (blockerId == null || blockedId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameters");
            return;
        }

        try {
            // Giả sử bạn có BlockDAO với hàm blockUser()
            boolean success = blockDAO.blockUser(Integer.parseInt(blockerId), Integer.parseInt(blockedId));
            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Block successful");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Block failed");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid ID format");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String blockerId = req.getParameter("blockerId");
        String blockedId = req.getParameter("blockedId");

        System.out.println("UnblockServlet: blockerId = " + blockerId + ", blockedId = " + blockedId);

        if (blockerId == null || blockedId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameters");
            return;
        }

        try {
            boolean success = blockDAO.unblockUser(Integer.parseInt(blockerId), Integer.parseInt(blockedId));
            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Unblock successful");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Unblock failed");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid ID format");
        }
    }
}
