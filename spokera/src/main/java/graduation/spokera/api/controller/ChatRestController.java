package graduation.spokera.api.controller;

import graduation.spokera.api.domain.type.MatchStatus;
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
import java.util.Map;
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
        // 리턴 우선순위
        Map<MatchStatus, Integer> statusPriority = Map.of(
                MatchStatus.MATCHED, 0,
                MatchStatus.WAITING, 1,
                MatchStatus.COMPLETED, 2
        );

        // status 우선순위 후에 날짜를 내림차순해서 정렬해서 보냄
        return matchParticipantRepository.findByUser(user)
                .stream()
                .map(MatchParticipant::getMatch)
                .sorted(
                        Comparator.comparingInt((Match m) ->
                                        statusPriority.getOrDefault(m.getStatus(), Integer.MAX_VALUE)
                                )
                                .thenComparing(Match::getStartTime, Comparator.reverseOrder())
                )
                .collect(Collectors.toList());
    }

}
