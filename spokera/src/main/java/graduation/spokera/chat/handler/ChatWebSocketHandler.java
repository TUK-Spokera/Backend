package graduation.spokera.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import graduation.spokera.chat.model.ChatMessage;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ConcurrentHashMap<WebSocketSession, String> users = new ConcurrentHashMap<>();
    private final AtomicInteger userCounter = new AtomicInteger(1);
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        String username = "User" + userCounter.getAndIncrement();
        users.put(session, username);

        logger.info("{} 채팅방 연결 : {}", username, session);
        boardcastMessage(username + " 님이 접속했습니다.");

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String username = users.get(session);
        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
        chatMessage.setSender(username);
        logger.debug("[chat] {} : {}", chatMessage.getSender(), chatMessage.getContent());

        for (WebSocketSession s : users.keySet()){
            s.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
        }


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = users.remove(session);

    }

    private void boardcastMessage(String content){
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setSender("시스템");
        systemMessage.setContent(content);

        try{
            for (WebSocketSession s : users.keySet()){
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(systemMessage)));
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
