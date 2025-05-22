<%-- 
    Document   : login
    Created on : May 17, 2025, 2:30:56 PM
    Author     : Thanh Loc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Login - Chat App</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f0f2f5;
            display: flex;
            height: 100vh;
            justify-content: center;
            align-items: center;
        }
        .login-container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            width: 300px;
        }
        h2 {
            text-align: center;
            margin-bottom: 20px;
        }
        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 10px;
            margin-bottom: 12px;
            border: 1px solid #ccc;
            border-radius: 6px;
        }
        input[type="submit"] {
            width: 100%;
            background: #4CAF50;
            color: white;
            padding: 10px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
        }
        input[type="submit"]:hover {
            background: #45a049;
        }
        .error {
            color: red;
            font-size: 14px;
            margin-bottom: 12px;
        }
    </style>
</head>
<body>
<div class="login-container">
    <h2>Đăng nhập</h2>
    <form action="login" method="post">
        <%
            String error = request.getParameter("error");
            if ("1".equals(error)) {
        %>
        <div class="error">Sai tài khoản hoặc mật khẩu!</div>
        <% } %>
        <input type="text" name="username" placeholder="Tên đăng nhập" required>
        <input type="password" name="password" placeholder="Mật khẩu" required>
        <input type="submit" value="Đăng nhập">
    </form>
</div>
</body>
</html>

