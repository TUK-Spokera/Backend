package graduation.spokera.api.controller;

import graduation.spokera.api.dto.user.UserLocationDTO;
import graduation.spokera.api.util.LocationMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@RequiredArgsConstructor
@Controller
public class LocationSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final LocationMemoryStore locationStore;

    @MessageMapping("/location.update")
    public void handleLocation(@Payload UserLocationDTO location,
                               @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {

        String userId = (String) sessionAttributes.get("userId");
        String teamId = (String) sessionAttributes.get("teamId");

        // 🔍 로그 추가
        System.out.println("📥 [위치 수신]");
        System.out.println("👤 userId: " + userId);
        System.out.println("👥 teamId: " + teamId);
        System.out.println("📍 좌표: (" + location.getLatitude() + ", " + location.getLongitude() + ")");

        location.setUserId(userId);
        location.setTimestamp(System.currentTimeMillis());

        // 1. 메모리에 위치 저장
        locationStore.updateLocation(userId, location);
        System.out.println("💾 메모리에 위치 저장 완료");

        // 2. 팀원들에게 위치 브로드캐스트
        messagingTemplate.convertAndSend("/topic/team/" + teamId, location);
        System.out.println("📡 브로드캐스트 완료 → /topic/team/" + teamId);
    }
}
