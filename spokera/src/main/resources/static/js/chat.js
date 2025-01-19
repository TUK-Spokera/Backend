let socket = new WebSocket("ws://localhost:8080/ws/chat");

socket.onopen = function () {
    console.log("WebSocket 연결이 성공적으로 설정되었습니다.");
};

socket.onmessage = function (event) {
    const chat = document.getElementById("chat");
    const message = JSON.parse(event.data);
    const messageElement = document.createElement("div");
    messageElement.textContent = `${message.sender}: ${message.content}`;
    chat.appendChild(messageElement);

    // 스크롤을 최신 메시지로 이동
    chat.scrollTop = chat.scrollHeight;
};

socket.onclose = function () {
    console.log("WebSocket 연결이 종료되었습니다.");
};

// 메시지 전송 함수
function sendMessage() {
    const input = document.getElementById("message");
    const messageText = input.value.trim();

    if (messageText !== "") {
        const message = {
            content: messageText,
        };
        socket.send(JSON.stringify(message)); // 메시지 전송
        input.value = ""; // 입력창 초기화
    }
}

// Enter 키 입력 시 메시지 전송
function checkEnter(event) {
    if (event.key === "Enter" && !event.isComposing) {
        event.preventDefault(); // 기본 Enter 동작 방지
        sendMessage(); // 메시지 전송
    }
}