package graduation.spokera.api.controller;

import graduation.spokera.api.dto.ChatMessageDTO;
import graduation.spokera.api.dto.MatchDTO;
import graduation.spokera.api.model.ChatMessage;
import graduation.spokera.api.model.Match;
import graduation.spokera.api.model.MatchParticipant;
import graduation.spokera.api.model.User;
import graduation.spokera.api.repository.ChatMessageRepository;
import graduation.spokera.api.repository.MatchParticipantRepository;
import graduation.spokera.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {

    private final MatchParticipantRepository matchParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /**
     * REST API: 채팅 내역 불러오기
     */
    @GetMapping("/history/{matchId}")
    public List<ChatMessageDTO> getChatHistory(@PathVariable Long matchId) {
        List<ChatMessage> messages = chatMessageRepository.findByMatch_MatchIdOrderBySentAtAsc(matchId);
        return messages.stream()
                .map(msg -> new ChatMessageDTO(msg.getMatch().getMatchId(), msg.getSender().getUsername(), msg.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 사용자가 속한 채팅방 목록 불러오기
     */
    @GetMapping("/rooms/{username}")
    public List<MatchDTO> getUserChatRooms(@PathVariable String username) {
        // 사용자를 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 사용자가 속한 매칭방 목록 조회
        List<Match> userMatches = matchParticipantRepository.findByUser(user)
                .stream()
                .map(MatchParticipant::getMatch)
                .collect(Collectors.toList());

        // DTO 변환하여 반환
        return userMatches.stream()
                .map(match -> new MatchDTO(match.getMatchId(), match.getSportType(), match.getStatus(), match.getStartTime(), match.getEndTime()))
                .collect(Collectors.toList());
    }
}