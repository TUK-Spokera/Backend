package graduation.spokera.api.controller;

import graduation.spokera.api.dto.chat.ChatMessageDTO;
import graduation.spokera.api.domain.chat.ChatMessage;
import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.repository.ChatMessageRepository;
import graduation.spokera.api.repository.MatchParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {

    private final MatchParticipantRepository matchParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * ✅ 채팅 내역 불러오기
     */
    @GetMapping("/history/{matchId}")
    public List<ChatMessageDTO> getChatHistory(@PathVariable Long matchId) {
        List<ChatMessage> messages = chatMessageRepository.findByMatch_MatchIdOrderBySentAtAsc(matchId);
        return messages.stream()
                .map(msg -> new ChatMessageDTO(
                        msg.getMatch().getMatchId(),
                        msg.getSender().getNickname(),
                        msg.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 현재 유저가 속한 채팅방 목록 불러오기
     */
    @GetMapping("/rooms")
    public List<Match> getUserChatRooms(@AuthenticationPrincipal User user) {
        return matchParticipantRepository.findByUser(user)
                .stream()
                .map(MatchParticipant::getMatch)
                .sorted(Comparator.comparing(Match::getStartTime).reversed())
                .collect(Collectors.toList());
    }

}
