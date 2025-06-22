package graduation.spokera.api.controller;

import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.dto.chat.ChatEnterDTO;
import graduation.spokera.api.dto.chat.ChatMessageDTO;
import graduation.spokera.api.domain.chat.ChatMessage;
import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.dto.chat.ChatTeamInfoRequest;
import graduation.spokera.api.dto.chat.ChatTeamInfoResponse;
import graduation.spokera.api.dto.vote.VoteMessageDTO;
import graduation.spokera.api.dto.vote.VoteResultResponseDTO;
import graduation.spokera.api.repository.ChatMessageRepository;
import graduation.spokera.api.repository.MatchParticipantRepository;
import graduation.spokera.api.repository.MatchRepository;
import graduation.spokera.api.service.FacilityVoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final FacilityVoteService facilityVoteService;

    @MessageMapping("/chat.teamInfo")
    public void sendTeamInfo(@Payload ChatTeamInfoRequest request) {
        Long matchId = request.getMatchId();

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("매칭방을 찾을 수 없습니다."));

        List<MatchParticipant> participants = matchParticipantRepository.findByMatch(match);

        Map<String, List<String>> teams = new HashMap<>();
        teams.put("RED", new ArrayList<>());
        teams.put("BLUE", new ArrayList<>());

        for (MatchParticipant participant : participants) {
            String team = participant.getTeam().name();
            teams.get(team).add(participant.getUser().getNickname());
        }

        ChatTeamInfoResponse teamInfo = new ChatTeamInfoResponse(
                "TEAM_INFO",
                matchId,
                teams
        );

        messagingTemplate.convertAndSend("/topic/room/" + matchId, teamInfo);
    }


    /**
     * ✅ 채팅방 입장 알림 전송
     */
    @MessageMapping("/chat.enter")
    public void enterChat(@Payload ChatTeamInfoRequest request, Principal principal) {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long matchId = request.getMatchId();

        ChatEnterDTO enterDTO = new ChatEnterDTO(
                matchId,
                user.getNickname(),
                user.getNickname() + "님이 입장했습니다."
        );

        messagingTemplate.convertAndSend("/topic/room/" + matchId, enterDTO);
    }



    /**
     * ✅ 채팅 메시지 전송
     */
    @MessageMapping("/chat.sendMessage")
    @Transactional
    public void sendMessage(@Payload ChatMessageDTO chatMessageDTO, Principal principal) {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        Match match = matchRepository.findById(chatMessageDTO.getMatchId())
                .orElseThrow(() -> new RuntimeException("매칭방을 찾을 수 없습니다."));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMatch(match);
        chatMessage.setSender(user);
        chatMessage.setContent(chatMessageDTO.getContent());
        chatMessage.setSentAt(LocalDateTime.now());

        chatMessageRepository.save(chatMessage);

        log.info("💬 [채팅 전송] matchId={}, sender={}, content={}",
                match.getMatchId(), user.getNickname(), chatMessage.getContent());

        messagingTemplate.convertAndSend("/topic/room/" + match.getMatchId(),
                new ChatMessageDTO(match.getMatchId(), user.getNickname(), chatMessage.getContent()));
    }

    /**
     * ✅ 경기장 투표 전송
     */
    @MessageMapping("/chat.sendVote")
    public void sendVote(VoteMessageDTO voteMessage, Principal principal) {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        Long matchId = voteMessage.getMatchId();
        String facilityName = voteMessage.getFacilityName();

        facilityVoteService.voteFacility(matchId, user.getId(), facilityName);

        Map<String, Integer> voteCounts = facilityVoteService.getVoteResult(matchId);
        String selectedFacility = facilityVoteService.getSelectedFacility(matchId);

        VoteResultResponseDTO result = new VoteResultResponseDTO(
                "VOTE_RESULT",
                matchId,
                voteCounts,
                selectedFacility
        );

        messagingTemplate.convertAndSend("/topic/room/" + matchId, result);
    }
}
