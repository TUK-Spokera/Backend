package graduation.spokera.api.controller;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.dto.user.UserLocationDTO;
import graduation.spokera.api.util.LocationMemoryStore;
import graduation.spokera.api.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class LocationSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final LocationMemoryStore locationStore;
    private final MatchRepository matchRepository;

    // ✅ 위치 메시지 수신
    @MessageMapping("/location.update")
    public void handleLocation(@Payload UserLocationDTO location,
                               Principal principal,
                               @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {

        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        String userId = String.valueOf(user.getId());
        String nickname = user.getNickname();
        String matchId = (String) sessionAttributes.get("matchId");

        // ❗ matchId 유효성 검사
        if (!matchRepository.existsById(Long.parseLong(matchId))) {
            System.out.println("❌ 잘못된 matchId → 전송 중단");
            return;
        }

        // 로그
        System.out.println("📥 [위치 수신]");
        System.out.println("👤 userId: " + userId);
        System.out.println("🧑 nickname: " + nickname);
        System.out.println("👥 matchId: " + matchId);
        System.out.println("📍 좌표: (" + location.getLatitude() + ", " + location.getLongitude() + ")");

        // DTO 세팅
        location.setUserId(userId);
        location.setMatchId(matchId);
        location.setUsername(nickname);
        location.setTimestamp(System.currentTimeMillis());

        locationStore.updateLocation(userId, location);

        messagingTemplate.convertAndSend("/topic/match/" + matchId, location);
        System.out.println("📤 전송 데이터: " + location);
    }
}
