function toggleChat() {
    let chatContainer = document.getElementById("chatContainer");
    chatContainer.style.display = (chatContainer.style.display === "none" || chatContainer.style.display === "") ? "block" : "none";
}

async function sendMessage() {
    let userInput = document.getElementById("userInput").value.trim();
    if (userInput === "") return;

    let requestData = {
        message: userInput,
//        database: databaseScript,
        instruction: "Chỉ trả lời dựa trên dữ liệu trong database. Nếu không có thông tin, hãy trả lời: 'Vui lòng hỏi câu liên quan đến danh sách xe ô tô có sẵn.'"
    };

    let chatBox = document.getElementById("chatBoxAI");
    chatBox.innerHTML += `<div><b>Bạn:</b> ${userInput}</div>`;
    document.getElementById("userInput").value = "";

    try {
        let response = await fetch("/ReLoop/s_chatBox", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(requestData)
        });

        let data = await response.json();
        let botResponse = data.response || "Không có phản hồi từ AI.";
        chatBox.innerHTML += `<div><b>ReLoop:</b> ${botResponse}</div>`;
    } catch (error) {
        console.error("Lỗi kết nối AI:", error);
        chatBox.innerHTML += `<div style="color:red;"><b>Lỗi:</b> Không thể kết nối AI!</div>`;
    }

    chatBox.scrollTop = chatBox.scrollHeight;
}

function handleKeyPresss(event) {
    if (event.key === "Enter") {
        event.preventDefault();
        sendMessage();
    }
}
