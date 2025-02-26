package graduation.spokera.api.controller;

import graduation.spokera.api.dto.chat.ChatMessageDTO;
import graduation.spokera.api.domain.chat.ChatMessage;
import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.chat.ChatMessageRepository;
import graduation.spokera.api.domain.match.MatchRepository;
import graduation.spokera.api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    /**
     * ✅ WebSocket 실시간 채팅 메시지 전송
     */
    @MessageMapping("/chat.sendMessage")
    @Transactional
    public void sendMessage(ChatMessageDTO chatMessageDTO) {
        Match match = matchRepository.findById(chatMessageDTO.getMatchId())
                .orElseThrow(() -> new RuntimeException("매칭방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(chatMessageDTO.getSenderName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMatch(match);
        chatMessage.setSender(user);
        chatMessage.setContent(chatMessageDTO.getContent());
        chatMessage.setSentAt(LocalDateTime.now());

        chatMessageRepository.save(chatMessage);

        // ✅ 특정 채팅방(`/topic/room/{matchId}`)으로 메시지 전송
        String destination = "/topic/room/" + chatMessageDTO.getMatchId();
        simpMessagingTemplate.convertAndSend(destination,
                new ChatMessageDTO(chatMessage.getMatch().getMatchId(), user.getUsername(), chatMessage.getContent()));
    }
}