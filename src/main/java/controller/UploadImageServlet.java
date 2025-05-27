/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Thanh Loc
 */
@MultipartConfig
public class UploadImageServlet extends HttpServlet implements IImg_source {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fromUserId = req.getParameter("fromUserId");
        String toUserId = req.getParameter("toUserId");

        List<String> imageUrls = new ArrayList<>();

        for (Part part : req.getParts()) {
            if (part.getName().equals("images") && part.getSize() > 0) {
                String filename = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                String uniqueName = UUID.randomUUID() + "_" + filename;
                File saveFile = new File(IMAGE_DIR, uniqueName);

                try (InputStream is = part.getInputStream(); FileOutputStream fos = new FileOutputStream(saveFile)) {
                    is.transferTo(fos);
                }

                String dbImagePath = "/images/" + uniqueName;
                imageUrls.add(dbImagePath);

            }
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Trả về mảng JSON: {"imageUrls": ["/images/xxx", "/images/yyy"]}
        String json = new Gson().toJson(Collections.singletonMap("imageUrls", imageUrls));
        resp.getWriter().write(json);
    }

}
