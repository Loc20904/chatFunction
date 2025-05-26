<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%@ page import="controller.User" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8" />
        <title>Chat</title>
        <style>
            body {
                font-family: "Segoe UI", sans-serif;
                margin: 0;
                padding: 20px;
                background-color: #f4f4f9;
                color: #333;
            }

            h2 {
                margin-bottom: 20px;
            }

            h3 {
                margin-top: 0;
                margin-bottom: 10px;
            }

            #userList {
                list-style: none;
                padding: 0;
                max-height: 300px;
                overflow-y: auto;
                background-color: #fff;
                border: 1px solid #ccc;
                border-radius: 8px;
                padding: 8px;
                margin-bottom: 20px;
            }

            #userList li {
                cursor: pointer;
                padding: 8px 12px;
                margin-bottom: 6px;
                border-radius: 6px;
                transition: background-color 0.2s, transform 0.1s;
            }

            #userList li:hover {
                background-color: #e6f0ff;
            }

            #userList li.selected {
                background-color: #d0e7ff;
                font-weight: bold;
                border-left: 4px solid #3399ff;
            }

            #chatBox {
                border: 1px solid #ccc;
                border-radius: 8px;
                height: 300px;
                overflow-y: auto;
                padding: 10px;
                background-color: #ffffff;
                margin-bottom: 12px;
            }

            .msg-sent {
                text-align: right;
                color: #007bff;
                margin: 4px 0;
            }

            .msg-received {
                text-align: left;
                color: #28a745;
                margin: 4px 0;
            }

            #messageInput {
                width: calc(100% - 90px);
                padding: 10px;
                border: 1px solid #ccc;
                border-radius: 6px;
                font-size: 14px;
                margin-right: 8px;
            }

            button {
                padding: 10px 16px;
                font-size: 14px;
                background-color: #007bff;
                border: none;
                color: white;
                border-radius: 6px;
                cursor: pointer;
                transition: background-color 0.2s;
            }

            button:hover {
                background-color: #0056b3;
            }

            .chat-container {
                display: flex;
                gap: 40px;
            }

            .chat-users, .chat-main {
                flex: 1;
            }
            .msg-sent, .msg-received {
                display: inline-block;
                max-width: 70%;
                padding: 8px 12px;
                margin: 6px 0;
                border-radius: 12px;
                background-color: #e6f0ff;
                position: relative;
                word-wrap: break-word;
                font-size: 14px;
            }

            .msg-sent {
                background-color: #d2e3fc;
                align-self: flex-end;
                float: right;
                clear: both;
                text-align: left;
            }

            .msg-received {
                background-color: #f1f1f1;
                align-self: flex-start;
                float: left;
                clear: both;
                text-align: left;
            }

            .msg-sent small, .msg-received small {
                display: block;
                color: gray;
                font-size: 11px;
                text-align: right;
                margin-top: 4px;
            }
            #userList li.user-unread {
                background-color: #ffe5b4 !important; /* Màu cam nhạt */
            }

            .msg-recalled {
                background-color: #f8f8f8;
                color: #888;
                font-style: italic;
            }

            .recall-btn {
                margin-left: 8px;
                padding: 4px 8px;
                font-size: 12px;
                background-color: #ff4444;
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
            }

            .recall-btn:hover {
                background-color: #cc0000;
            }
        </style>
    </head>
    <body>
        <h2>Welcome, <%= user.getUsername()%></h2>

        <div class="chat-container">
            <div class="chat-users">
                <h3>Users</h3>
                <ul id="userList">
                    <c:forEach var="u" items="${userList}">
                        <c:if test="${u.userId != user.userId}">
                            <li data-userid="${u.userId}" onclick="selectChatUser(${u.userId}, '${u.username}')">
                                ${u.username}
                            </li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>

            <div class="chat-main">
                <h3>
                    Chat with <span id="chatWith">[Select user]</span>
                    <button id="blockBtn" onclick="blockUser()" style="display:none; margin-left: 10px;">Block</button>
                    <button id="unblockBtn" onclick="unblockUser()" style="display:none; margin-left: 5px;">Unblock</button>
                </h3>

                <div id="chatBox"></div>

                <div class="inputBox" style="display: flex; align-items: center; gap: 10px;">
                    <input type="text" id="messageInput" placeholder="Type a message..." onkeydown="handleKeyPress(event)" style="flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 6px; font-size: 14px;" />
                    <label id="imageUploadLabel" for="imageUpload" style="cursor: pointer;">
                        📷
                    </label>
                    <input type="file" id="imageUpload" style="display: none;" accept="image/*" onchange="sendImage()" />
                    <button onclick="sendMessage()" style="padding: 10px 16px; font-size: 14px; background-color: #007bff; border: none; color: white; border-radius: 6px; cursor: pointer;">Send</button>
                </div>
                <p id="blockNotice" style="color: red; font-weight: bold; display: none;">Bạn đã block người dùng này.</p>
            </div>
        </div>

        <script>
            const currentUserId = <%= user.getUserId()%>;
            const currentUsername = "<%= user.getUsername()%>";
            let currentChatUserId = null;
            let currentChatUserName = null;

            const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 1));

            const ws = new WebSocket("ws://localhost:8080/testChat/chat");

            ws.onopen = () => console.log("WebSocket connected");
            ws.onclose = () => console.log("WebSocket closed");
            ws.onerror = err => console.error("WebSocket error", err);

            const lastNotificationTimestamps = {};
            const NOTIFY_COOLDOWN_MS = 500; // 0,5 giây

            ws.onmessage = function (event) {
                const msg = JSON.parse(event.data);
                if (msg.type === "unread_list") {
                    msg.senders.forEach(senderId => {
                        markUserAsUnread(senderId); // Thêm class 'user-unread'
                    });
                    return;
                }
                if (msg.type === "block_status") {
                    const messageInput = document.getElementById("messageInput");
                    const sendBtn = document.querySelector(".inputBox button");
                    const blockBtn = document.getElementById("blockBtn");
                    const unblockBtn = document.getElementById("unblockBtn");
                    const blockNotice = document.getElementById("blockNotice");

                    if (
                            (msg.status === "blocked_by_me" || msg.status === "unblocked_by_me") &&
                            currentChatUserId !== msg.blockedId
                            )
                        return;

                    if (
                            (msg.status === "blocked_me" || msg.status === "unblocked_me") &&
                            currentChatUserId !== msg.blockerId
                            )
                        return;

                    if (msg.status === "blocked_by_me" || msg.status === "blocked_me") {
                        messageInput.style.display = "none";
                        sendBtn.style.display = "none";
                        blockBtn.style.display = "none";
                        document.getElementById("imageUploadLabel").style.display = "none";
                        document.getElementById("imageUpload").style.display = "none";
                        if (msg.status === "blocked_by_me") {
                            unblockBtn.style.display = "inline-block";
                        } else {
                            unblockBtn.style.display = "none";
                        }
                        blockNotice.textContent = msg.status === "blocked_by_me"
                                ? "Bạn đã block người dùng này"
                                : "Người dùng này đã block bạn";
                        blockNotice.style.display = "block";
                    } else if (msg.status === "unblocked_by_me" || msg.status === "unblocked_me") {
                        messageInput.style.display = "block";
                        sendBtn.style.display = "inline-block";
                        blockBtn.style.display = "inline-block";
                        unblockBtn.style.display = "none";
                        blockNotice.style.display = "none";
                        document.getElementById("imageUploadLabel").style.display = "inline-block";
                        document.getElementById("imageUpload").style.display = "none";
                    }
                    return;
                }

                if (msg.type === "recall") {
                    handleRecallMessage(msg);
                    return;
                }

                if (msg.type === "recall_failed") {
                    alert(msg.message);
                    return;
                }

                const isRelated =
                        (msg.fromUserId === currentChatUserId && msg.toUserId === currentUserId) ||
                        (msg.toUserId === currentChatUserId && msg.fromUserId === currentUserId);

                const isSentByMe = msg.fromUserId === currentUserId;

                if (isRelated) {
                    addMessageToChatBox(msg);
                }

                if (!isSentByMe && (!isRelated || document.hidden)) {
                    const now = Date.now();
                    const lastTime = lastNotificationTimestamps[msg.fromUserId] || 0;

                    if (now - lastTime > NOTIFY_COOLDOWN_MS) {
                        showBrowserNotification(msg.fromUsername, msg.content);
                        lastNotificationTimestamps[msg.fromUserId] = now;
                    }
                }

                if (!isSentByMe && msg.fromUserId !== currentChatUserId) {
                    markUserAsUnread(msg.fromUserId);
                } else
                {
                    markMessagesAsRead(msg.fromUserId);
                }
            };

            function selectChatUser(userId, username) {
                currentChatUserId = userId;
                currentChatUserName = username;
                document.getElementById("chatWith").textContent = username;

                document.querySelectorAll("#userList li").forEach(item => {
                    item.classList.toggle("selected", parseInt(item.dataset.userid) === userId);
                });

                document.getElementById("messageInput").style.display = "block";
                document.querySelector(".inputBox button").style.display = "inline-block";
                document.getElementById("blockNotice").style.display = "none";

                loadChatHistory(userId);
                markMessagesAsRead(userId);

                fetch("/testChat/checkBlock?user1=" + currentUserId + "&user2=" + userId)
                        .then(res => res.json())
                        .then(data => {
                            const blockBtn = document.getElementById("blockBtn");
                            const unblockBtn = document.getElementById("unblockBtn");
                            const messageInput = document.getElementById("messageInput");
                            const sendBtn = document.querySelector(".inputBox button");
                            const blockNotice = document.getElementById("blockNotice");

                            if (data.blockedByMe) {
                                blockBtn.style.display = "none";
                                unblockBtn.style.display = "inline-block";
                                messageInput.style.display = "none";
                                sendBtn.style.display = "none";
                                blockNotice.style.display = "block";
                                blockNotice.textContent = "Bạn đã block người dùng này";
                            } else if (data.blockedMe) {
                                blockBtn.style.display = "none";
                                unblockBtn.style.display = "none";
                                messageInput.style.display = "none";
                                sendBtn.style.display = "none";
                                blockNotice.style.display = "block";
                                blockNotice.textContent = "Bạn đã bị block bởi người dùng này";
                            } else {
                                blockBtn.style.display = "inline-block";
                                unblockBtn.style.display = "none";
                                messageInput.style.display = "block";
                                sendBtn.style.display = "inline-block";
                                blockNotice.style.display = "none";
                            }
                        })
                        .catch(err => {
                            console.error("Failed to check block status", err);
                            document.getElementById("blockNotice").textContent = "Không thể kiểm tra trạng thái block.";
                            document.getElementById("blockNotice").style.display = "block";
                        });
            }

            function addMessageToChatBox(msg) {
                if (msg.type === "image")
                {
                    addImageToChatBox(msg);
                    console.log("ajsdkahjshd");
                    return;
                }
                const chatBox = document.getElementById("chatBox");
                const p = document.createElement("p");
                const isSentByMe = (msg.fromUserId !== undefined && msg.fromUserId === currentUserId) ||
                        (msg.senderId !== undefined && msg.senderId === currentUserId);
                const content = msg.content;
                const msID = msg.messageId;
                let timeText = "";

                if (msg.timestamp) {
                    const time = new Date(msg.timestamp);
                    if (!isNaN(time)) {
                        timeText = '<small style="color:gray">' + time.toString().substring(0, 24) + '</small>';
                    } else {
                        timeText = '<small style="color:red">Invalid Date</small>';
                        console.error("Invalid timestamp received:", msg.timestamp);
                    }
                }

                const isRecalled = msg.is_recall === true || msg.type === "recall";

                if (isRecalled) {
                    p.className = isSentByMe ? "msg-sent" : "msg-received";
                    const senderName = isSentByMe ? "" : currentChatUserName + ":";
                    p.innerHTML = "<b>" + senderName + "</b> Tin nhắn đã được thu hồi<br><small style=\"color:gray\">" + timeText + "</small>";
                    chatBox.appendChild(p);
                    chatBox.scrollTop = chatBox.scrollHeight;
                    return;
                }

                p.className = isSentByMe ? "msg-sent" : "msg-received";
                p.dataset.messageId = msID || ""; // Đảm bảo dataset.messageId luôn có giá trị
                let messageContent = "<b>" + (isSentByMe ? "" : currentChatUserName + ":") + "</b> " + content + "<br>" + timeText;
                if (isSentByMe && msID !== undefined && msID !== null && (typeof msID === "string" || typeof msID === "number")) {
                    const validMsID = (typeof msID === "string") ? msID.trim() : msID.toString();
                    if (validMsID && !isNaN(Number(validMsID)) && Number(validMsID) > 0) {
                        messageContent += "<button class=\"recall-btn\" data-message-id=\"" + msID + "\" onclick=\"recallMessage(this)\">Recall</button>";
                    } else {
                        console.warn("messageId không hợp lệ, không thêm nút Recall:", msID);
                    }
                } else if (isSentByMe) {
                    console.warn("Không thể tạo nút Recall vì messageId không hợp lệ:", msID);
                }

                p.innerHTML = messageContent;
                chatBox.appendChild(p);
                chatBox.scrollTop = chatBox.scrollHeight;
            }

            function addImageToChatBox(msg) {
                const chatBox = document.getElementById("chatBox");
                const msID = msg.messageId;

                if (msg.type !== "image") {
                    console.warn("Message type is not image, ignoring:", msg);
                    return;
                }

                const isSentByMe = (msg.fromUserId !== undefined && msg.fromUserId === currentUserId) ||
                        (msg.senderId !== undefined && msg.senderId === currentUserId);

                const isRecalled = msg.is_recall === true || msg.type === "recall";

                let timeText = "";
                if (msg.timestamp) {
                    const time = new Date(msg.timestamp);
                    if (!isNaN(time)) {
                        timeText = '<small style="color:gray">' + time.toString().substring(0, 24) + '</small>';
                    } else {
                        timeText = '<small style="color:red">Invalid Date</small>';
                        console.error("Invalid timestamp received:", msg.timestamp);
                    }
                }

                const p = document.createElement("p");
                p.className = isSentByMe ? "msg-sent" : "msg-received";
                p.dataset.messageId = msID || "";

                if (isRecalled) {
                    const senderName = isSentByMe ? "" : (msg.fromUsername || currentChatUserName) + ":";
                    p.innerHTML = "<b>" + senderName + "</b> Tin nhắn đã được thu hồi<br>" + timeText;
                } else {
                    // Tạo phần ảnh
                    const img = document.createElement("img");
                    img.src = contextPath + msg.content;
                    img.style.maxWidth = "200px";
                    img.style.borderRadius = "8px";

                    p.appendChild(img);

                    // Tạo nội dung thời gian và nút Recall theo định dạng bạn đưa
                    let messageContent = "<br><b>" + (isSentByMe ? "" : (msg.fromUsername || currentChatUserName) + ":") + "</b><br>" + timeText;

                    const msID = msg.messageId;
                    if (isSentByMe && msID !== undefined && msID !== null && (typeof msID === "string" || typeof msID === "number")) {
                        const validMsID = (typeof msID === "string") ? msID.trim() : msID.toString();
                        if (validMsID && !isNaN(Number(validMsID)) && Number(validMsID) > 0) {
                            messageContent += "<button class=\"recall-btn\" data-message-id=\"" + msID + "\" onclick=\"recallMessage(this)\">Recall</button>";
                        } else {
                            console.warn("messageId không hợp lệ, không thêm nút Recall:", msID);
                        }
                    } else if (isSentByMe) {
                        console.warn("Không thể tạo nút Recall vì messageId không hợp lệ:", msID);
                    }

                    const divContent = document.createElement("div");
                    divContent.innerHTML = messageContent;
                    p.appendChild(divContent);
                }

                chatBox.appendChild(p);
                chatBox.scrollTop = chatBox.scrollHeight;
            }

            function handleRecallMessage(msg) {
                const isSentByMe = (msg.fromUserId !== undefined && msg.fromUserId === currentUserId) ||
                        (msg.senderId !== undefined && msg.senderId === currentUserId);
                const messageElement = document.querySelector("#chatBox p[data-message-id='" + msg.messageId + "']");
                if (messageElement !== null) {
                    messageElement.className = isSentByMe ? "msg-sent" : "msg-received";
                    const senderName = isSentByMe ? "" : currentChatUserName + ":";
                    const time = new Date(msg.timestamp);
                    const timeText = isNaN(time) ? "Invalid Date" : time.toString().substring(0, 24);
                    messageElement.innerHTML = "<b>" + senderName + "</b> Tin nhắn đã được thu hồi<br><small style=\"color:gray\">" + timeText + "</small>";
                } else {
                    console.error("Message element not found for messageId:", msg.messageId);
                }
            }

            function sendMessage() {
                const input = document.getElementById("messageInput");
                const content = input.value.trim();
                if (!currentChatUserId)
                    return alert("Select a user to chat with");
                if (!content)
                    return alert("Message cannot be empty");

                const msg = {
                    type: "message",
                    fromUserId: currentUserId,
                    fromUsername: currentUsername,
                    toUserId: currentChatUserId,
                    content: content,
                    timestamp: new Date()
                };

                ws.send(JSON.stringify(msg));
                input.value = "";
            }

            function handleKeyPress(event) {
                if (event.key === "Enter") {
                    sendMessage();
                }
            }

            function loadChatHistory(userId) {
                fetch("/testChat/GetChatHistory?user1=" + currentUserId + "&user2=" + userId)
                        .then(response => {
                            if (!response.ok) {
                                response.text().then(text => console.error("Error body:", text));
                                throw new Error(`HTTP error! status: ${response.status}`);
                            }
                            return response.json();
                        })
                        .then(data => {
                            const chatBox = document.getElementById("chatBox");
                            chatBox.innerHTML = "";
                            data.forEach(addMessageToChatBox);
                        })
                        .catch(err => {
                            console.error("Failed to load chat history", err);
                            document.getElementById("chatBox").innerHTML = "<p style='color: red;'>Failed to load chat history.</p>";
                        });
            }

            if ("Notification" in window && Notification.permission !== "granted") {
                Notification.requestPermission().then(permission => {
                    if (permission !== "granted") {
                        console.log("Notification permission denied");
                    }
                });
            }

            function showBrowserNotification(username, content) {
                if (Notification.permission === "granted") {
                    const notification = new Notification("New message from " + username, {
                        body: content,
                        icon: "https://uxwing.com/wp-content/themes/uxwing/download/communication-chat-call/new-message-icon.png"
                    });
                    notification.onclick = () => {
                        window.focus();
                    };
                }
            }

            function markUserAsUnread(userId) {
                const li = [...document.querySelectorAll("#userList li")].find(li => li.dataset.userid === userId.toString());
                if (li && !li.classList.contains("user-unread")) {
                    li.classList.add("user-unread");
                }
            }

            function markMessagesAsRead(fromUserId) {
                const message = {
                    type: "read",
                    fromUserId: fromUserId
                };
                ws.send(JSON.stringify(message));
                clearUnreadMark(fromUserId);
            }

            function clearUnreadMark(userId) {
                const li = [...document.querySelectorAll("#userList li")].find(li => li.dataset.userid === userId.toString());
                if (li) {
                    li.classList.remove("user-unread");
                }
            }


            function blockUser() {
                if (!currentChatUserId || !ws || ws.readyState !== ws.OPEN)
                    return;

                const message = {
                    type: "block",
                    fromUserId: currentUserId,
                    blockedId: currentChatUserId
                };

                ws.send(JSON.stringify(message));

                document.getElementById("blockBtn").style.display = "none";
                document.getElementById("unblockBtn").style.display = "inline-block";
                document.getElementById("messageInput").style.display = "none";
                document.querySelector(".inputBox button").style.display = "none";
                document.getElementById("blockNotice").style.display = "block";
                document.getElementById("blockNotice").textContent = "Bạn đã block người dùng này";
                document.getElementById("imageUploadLabel").style.display = "none";
                document.getElementById("imageUpload").style.display = "none";
            }


            function unblockUser() {
                if (!currentChatUserId || !ws || ws.readyState !== ws.OPEN)
                    return;

                const message = {
                    type: "unblock",
                    fromUserId: currentUserId,
                    blockedId: currentChatUserId
                };

                ws.send(JSON.stringify(message));

                document.getElementById("blockBtn").style.display = "inline-block";
                document.getElementById("unblockBtn").style.display = "none";
                document.getElementById("messageInput").style.display = "block";
                document.querySelector(".inputBox button").style.display = "inline-block";
                document.getElementById("blockNotice").style.display = "none";
                document.getElementById("imageUploadLabel").style.display = "inline-block";
                document.getElementById("imageUpload").style.display = "none";
            }

            function recallMessage(button) {
                const rawId = button.dataset.messageId;
                const messageId = rawId ? Number(rawId.trim()) : NaN;

                if (!currentChatUserId) {
                    alert("Vui lòng chọn người dùng để trò chuyện");
                    return;
                }
                if (!rawId || isNaN(messageId) || messageId <= 0) {
                    alert("ID tin nhắn không hợp lệ");
                    return;
                }

                if (confirm("Bạn có chắc muốn thu hồi tin nhắn này?")) {
                    const recallMsg = {
                        type: "recall",
                        messageId: messageId,
                        fromUserId: currentUserId,
                        toUserId: currentChatUserId
                    };
                    console.log("Sending recall message:", recallMsg);
                    ws.send(JSON.stringify(recallMsg));
                }
            }

            function sendImage() {
                const input = document.getElementById("imageUpload");
                const file = input.files[0];
                const formData = new FormData();
                formData.append("image", file);
                formData.append("fromUserId", currentUserId); // bạn cần gán đúng
                formData.append("toUserId", currentChatUserId);

                fetch("/testChat/uploadImage", {
                    method: "POST",
                    body: formData
                })
                        .then(response => response.json())
                        .then(result => {
                            const imageUrl = result.imageUrl;

                            // Sau khi upload thành công, gửi ảnh qua WebSocket
                            ws.send(JSON.stringify({
                                type: "image",
                                fromUserId: currentUserId,
                                toUserId: currentChatUserId,
                                imageUrl: imageUrl
                            }));
                        })
                        .catch(error => console.error("Upload failed", error));
            }
        </script>
    </body>
</html>