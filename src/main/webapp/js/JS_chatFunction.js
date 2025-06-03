
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

                const imageUploadl = document.getElementById("imageUploadLabel");
                imageUploadl.style.display = "none";
                const imageUpload = document.getElementById("imageUpload");
                imageUpload.style.display = "none";

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
                    imageUpload.style.display = "None";
                    imageUploadl.style.display = "inline-block";
                }

            })

            .catch(err => {

                console.error("Failed to check block status", err);

                document.getElementById("blockNotice").textContent = "Kh뿯½ng th뿯ẽ ki뿯ẽm tra tr뿯ẽng th뿯½i block.";

                document.getElementById("blockNotice").style.display = "block";

            });

}

function addMessageToChatBox(msg) {
    if (msg.type === "image") {
        addImageToChatBox(msg);
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
        p.className = isSentByMe ? "msg-sent msg-recalled" : "msg-received msg-recalled";
        const senderName = isSentByMe ? "" : currentChatUserName + ":";
        p.innerHTML = "<b>" + senderName + "</b> Tin nhắn đã được thu hồi<br><small style=\"color:gray\">" + timeText + "</small>";
        chatBox.appendChild(p);
        chatBox.scrollTop = chatBox.scrollHeight;
        return;
    }

    p.className = isSentByMe ? "msg-sent" : "msg-received";
    p.dataset.messageId = msID || "";
    p.style.position = "relative";

    let messageContent = "<b>" + (isSentByMe ? "" : currentChatUserName + ":") + "</b> " + content + "<br>" + timeText;

    let dropdownHTML = `
        <div class="dropdown" style="position: absolute; top: 0; right: 0;">
            <button class="dropdown-btn">⋮</button>
            <div class="dropdown-content">
                <button onclick="copyMessage('${encodeURIComponent(content)}', this)">Copy</button>
    `;
    if (isSentByMe && msID !== undefined && msID !== null && (typeof msID === "string" || typeof msID === "number")) {
        const validMsID = (typeof msID === "string") ? msID.trim() : msID.toString();
        if (validMsID && !isNaN(Number(validMsID)) && Number(validMsID) > 0) {
            dropdownHTML += `<button onclick="recallMessage('${msID}')">Recall</button>`;
        } else {
            console.warn("messageId không hợp lệ, không thêm nút Recall:", msID);
        }
    }
    dropdownHTML += `
            </div>
        </div>
    `;

    p.innerHTML = messageContent + dropdownHTML;
    chatBox.appendChild(p);
    chatBox.scrollTop = chatBox.scrollHeight;

    const dropdownBtn = p.querySelector(".dropdown-btn");
    const dropdownContent = p.querySelector(".dropdown-content");
    dropdownBtn.addEventListener("click", function (e) {
        e.stopPropagation();
        const isOpen = dropdownContent.style.display === "block";
        document.querySelectorAll(".dropdown-content").forEach(content => content.style.display = "none");
        dropdownContent.style.display = isOpen ? "none" : "block";
    });
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
    p.style.position = "relative"; // To position the dropdown

    if (isRecalled) {
        p.className += " msg-recalled";
        const senderName = isSentByMe ? "" : (msg.fromUsername || currentChatUserName) + ":";
        p.innerHTML = "<b>" + senderName + "</b> Tin nhắn đã được thu hồi<br>" + timeText;
        return;
    } else {
        const img = document.createElement("img");
        img.src = contextPath + msg.content;
        img.style.maxWidth = "200px";
        img.style.borderRadius = "8px";

        p.appendChild(img);

        let messageContent = "<br><b>" + (isSentByMe ? "" : (msg.fromUsername || currentChatUserName) + ":") + "</b><br>" + timeText;

        // Create dropdown menu
        let dropdownHTML = `
        <div class="dropdown" style="position: absolute; top: 0; right: 0;">
            <button class="dropdown-btn">⋮</button>
            <div class="dropdown-content">
                <button onclick="copyMessage('${encodeURIComponent(img)}', this)">Copy</button>
    `;
        if (isSentByMe && msID !== undefined && msID !== null && (typeof msID === "string" || typeof msID === "number")) {
            const validMsID = (typeof msID === "string") ? msID.trim() : msID.toString();
            if (validMsID && !isNaN(Number(validMsID)) && Number(validMsID) > 0) {
                dropdownHTML += `<button onclick="recallMessage('${msID}')">Recall</button>`;
            } else {
                console.warn("messageId không hợp lệ, không thêm nút Recall:", msID);
            }
        }
        dropdownHTML += `
                </div>
            </div>
        `;

        const divContent = document.createElement("div");
        divContent.innerHTML = messageContent + dropdownHTML;
        p.appendChild(divContent);
    }

    chatBox.appendChild(p);
    chatBox.scrollTop = chatBox.scrollHeight;

    //fix in here can not find dropdownBtn
    const dropdownBtn = p.querySelector(".dropdown-btn");
    const dropdownContent = p.querySelector(".dropdown-content");

    if (dropdownBtn && dropdownContent) {
        dropdownBtn.addEventListener("click", function (e) {
            e.stopPropagation();
            const isOpen = dropdownContent.style.display === "block";
            document.querySelectorAll(".dropdown-content").forEach(content => content.style.display = "none");
            dropdownContent.style.display = isOpen ? "none" : "block";
        });
    } else {
        console.warn("Dropdown button hoặc dropdown content không tìm thấy trong addImageToChatBox:", {
            dropdownBtn,
            dropdownContent,
            messageId: msg.messageId
        });
    }

}



function handleRecallMessage(msg) {

    const isSentByMe = (msg.fromUserId !== undefined && msg.fromUserId === currentUserId) ||
            (msg.senderId !== undefined && msg.senderId === currentUserId);

    const messageElement = document.querySelector("#chatBox p[data-message-id='" + msg.messageId + "']");

    if (messageElement !== null) {

        messageElement.className = isSentByMe ? "msg-sent msg-recalled" : "msg-received msg-recalled";

        const senderName = isSentByMe ? "" : currentChatUserName + ":";

        const time = new Date(msg.timestamp);

        const timeText = isNaN(time) ? "Invalid Date" : time.toString().substring(0, 24);

        messageElement.innerHTML = "<b>" + senderName + "</b> Tin nhắn đã được thu hồi<br><small style=\"color:gray\">" + timeText + "</small>";

    } else {

        console.error("Message element not found for messageId:", msg.messageId);

    }

}



function sendMessages() {

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
        sendMessages();
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

function recallMessage(meId) {

//    const rawId = button.dataset.messageId;
//
//    const messageId = rawId ? Number(rawId.trim()) : NaN;

    let messageId = parseInt(meId);

    if (!currentChatUserId) {

        alert("Vui l뿯½ng ch뿯ẽn ng뿯ƽ뿯ẽi d뿯½ng đ뿯ẽ tr뿯½ chuy뿯ẽn");

        return;

    }

    if (isNaN(messageId) || messageId <= 0) {

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

        ws.send(JSON.stringify(recallMsg));

    }

}

function sendImage() {
    const input = document.getElementById("imageUpload");
    const files = input.files;

    if (files.length === 0)
        return;

    const formData = new FormData();

    for (let i = 0; i < files.length; i++) {
        formData.append("images", files[i]); // name="images" để trùng servlet xử lý
    }

    formData.append("fromUserId", currentUserId);
    formData.append("toUserId", currentChatUserId);

    fetch("/testChat/uploadImage", {
        method: "POST",
        body: formData
    })
            .then(response => response.json())
            .then(result => {
                const imageUrls = result.imageUrls; // giả sử server trả về list ảnh

                imageUrls.forEach(imageUrl => {
                    ws.send(JSON.stringify({
                        type: "image",
                        fromUserId: currentUserId,
                        toUserId: currentChatUserId,
                        imageUrl: imageUrl
                    }));
                });
            })
            .catch(error => console.error("Upload failed", error));
}
function searchUsers() {
    const searchInput = document.getElementById("search").value.toLowerCase();
    const userList = document.getElementById("userList");
    const users = userList.getElementsByTagName("li");

    for (let i = 0; i < users.length; i++) {
        const username = users[i].textContent.toLowerCase();
        if (username.includes(searchInput)) {
            users[i].style.display = "";
        } else {
            users[i].style.display = "none";
        }
    }
}

// Add event listener for search input
document.getElementById("search").addEventListener("input", searchUsers);

// Add event listener to close dropdown when clicking outside
document.addEventListener("click", function (event) {
    const dropdowns = document.getElementsByClassName("dropdown-content");
    for (let i = 0; i < dropdowns.length; i++) {
        const openDropdown = dropdowns[i];
        if (openDropdown.style.display === "block" && !event.target.closest(".dropdown")) {
            openDropdown.style.display = "none";
        }
    }
});

function copyMessage(content) {
    navigator.clipboard.writeText(content)
            .then(() => {
                //alert("Message copied to clipboard!");
            })
            .catch(err => {
                console.error("Failed to copy message:", err);
                //alert("Failed to copy message.");
            });
}