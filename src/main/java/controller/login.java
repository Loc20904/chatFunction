/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Thanh Loc
 */
public class login extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password"); // Ở đây bạn phải hash rồi so sánh thực tế
        System.out.println("Hello ");
        User user = UserDAO.getUserByUsername(username);

        if (user != null && PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            req.getSession().setAttribute("user", user);
            resp.sendRedirect("UsersServlet");
        } else {
            resp.sendRedirect("login.jsp?error=1");
        }
    }
}

