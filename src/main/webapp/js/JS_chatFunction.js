
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

                                blockNotice.textContent = "Bạn đã bị người dùng này block";

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

                            document.getElementById("blockNotice").textContent = "Kh뿯½ng th뿯ẽ ki뿯ẽm tra tr뿯ẽng th뿯½i block.";

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

                p.dataset.messageId = msID || ""; // Đ뿯ẽm b뿯ẽo dataset.messageId lu뿯½n c뿯½ gi뿯½ tr뿯ẽ

                let messageContent = "<b>" + (isSentByMe ? "" : currentChatUserName + ":") + "</b> " + content + "<br>" + timeText;

                if (isSentByMe && msID !== undefined && msID !== null && (typeof msID === "string" || typeof msID === "number")) {

                    const validMsID = (typeof msID === "string") ? msID.trim() : msID.toString();

                    if (validMsID && !isNaN(Number(validMsID)) && Number(validMsID) > 0) {

                        messageContent += "<button class=\"recall-btn\" data-message-id=\"" + msID + "\" onclick=\"recallMessage(this)\">Recall</button>";

                    } else {

                        console.warn("messageId kh뿯½ng h뿯ẽp l뿯ẽ, kh뿯½ng th뿯½m n뿯½t Recall:", msID);

                    }

                } else if (isSentByMe) {

                    console.warn("Kh뿯½ng th뿯ẽ t뿯ẽo n뿯½t Recall v뿯½ messageId kh뿯½ng h뿯ẽp l뿯ẽ:", msID);

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

                    // T뿯ẽo ph뿯ẽn 뿯ẽnh

                    const img = document.createElement("img");

                    img.src = contextPath + msg.content;

                    img.style.maxWidth = "200px";

                    img.style.borderRadius = "8px";



                    p.appendChild(img);



                    // T뿯ẽo n뿯ẽi dung th뿯ẽi gian v뿯½ n뿯½t Recall theo đ뿯ẽnh d뿯ẽng b뿯ẽn đ뿯ƽa

                    let messageContent = "<br><b>" + (isSentByMe ? "" : (msg.fromUsername || currentChatUserName) + ":") + "</b><br>" + timeText;



                    const msID = msg.messageId;

                    if (isSentByMe && msID !== undefined && msID !== null && (typeof msID === "string" || typeof msID === "number")) {

                        const validMsID = (typeof msID === "string") ? msID.trim() : msID.toString();

                        if (validMsID && !isNaN(Number(validMsID)) && Number(validMsID) > 0) {

                            messageContent += "<button class=\"recall-btn\" data-message-id=\"" + msID + "\" onclick=\"recallMessage(this)\">Recall</button>";

                        } else {

                            console.warn("messageId kh뿯½ng h뿯ẽp l뿯ẽ, kh뿯½ng th뿯½m n뿯½t Recall:", msID);

                        }

                    } else if (isSentByMe) {

                        console.warn("Kh뿯½ng th뿯ẽ t뿯ẽo n뿯½t Recall v뿯½ messageId kh뿯½ng h뿯ẽp l뿯ẽ:", msID);

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

                    alert("Vui l뿯½ng ch뿯ẽn ng뿯ƽ뿯ẽi d뿯½ng đ뿯ẽ tr뿯½ chuy뿯ẽn");

                    return;

                }

                if (!rawId || isNaN(messageId) || messageId <= 0) {

                    alert("ID tin nh뿯ẽn kh뿯½ng h뿯ẽp l뿯ẽ");

                    return;

                }



                if (confirm("Bạn có chắc muốn gở tin nhắn này ?")) {

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

                formData.append("fromUserId", currentUserId); // b뿯ẽn c뿯ẽn g뿯½n đ뿯½ng

                formData.append("toUserId", currentChatUserId);



                fetch("/testChat/uploadImage", {

                    method: "POST",

                    body: formData

                })

                        .then(response => response.json())

                        .then(result => {

                            const imageUrl = result.imageUrl;



                            // Sau khi upload th뿯½nh c뿯½ng, g뿯ẽi 뿯ẽnh qua WebSocket

                            ws.send(JSON.stringify({

                                type: "image",

                                fromUserId: currentUserId,

                                toUserId: currentChatUserId,

                                imageUrl: imageUrl

                            }));

                        })

                        .catch(error => console.error("Upload failed", error));

            }