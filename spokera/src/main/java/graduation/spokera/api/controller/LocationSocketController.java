package graduation.spokera.api.controller;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.dto.user.UserLocationDTO;
import graduation.spokera.api.util.LocationMemoryStore;
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

    @MessageMapping("/location.update")
    public void handleLocation(@Payload UserLocationDTO location,
                               Principal principal,
                               @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {

        // 🔐 사용자 정보
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        String userId = String.valueOf(user.getId());
        String nickname = user.getNickname();

        // 🧠 teamId는 CONNECT 시 세션에 저장된 값
        String teamId = (String) sessionAttributes.get("teamId");

        // 📝 로그
        System.out.println("📥 [위치 수신]");
        System.out.println("👤 userId: " + userId);
        System.out.println("🧑 nickname: " + nickname);
        System.out.println("👥 teamId: " + teamId);
        System.out.println("📍 좌표: (" + location.getLatitude() + ", " + location.getLongitude() + ")");

        // 🛠 DTO에 추가 정보 세팅
        location.setUserId(userId);
        location.setUsername(nickname);
        location.setTimestamp(System.currentTimeMillis());

        // 💾 메모리 저장
        locationStore.updateLocation(userId, location);

        // 📡 브로드캐스트
        messagingTemplate.convertAndSend("/topic/team/" + teamId, location);
        System.out.println("📤 전송 데이터: " + location);

    }
}
