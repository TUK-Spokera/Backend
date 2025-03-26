package graduation.spokera.api.util;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.user.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public StompAuthChannelInterceptor(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            String teamId = accessor.getFirstNativeHeader("teamId"); // ⬅ teamId도 받기

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                if (jwtUtil.validateToken(token)) {
                    String userId = jwtUtil.getUserIdFromToken(token);

                    // 🔍 DB에서 유저 정보 조회
                    User user = userRepository.findById(Long.parseLong(userId))
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    // ✅ Principal로 User 전체 넣기
                    accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, List.of()));

                    // ✅ teamId 세션에 저장
                    if (teamId != null) {
                        accessor.getSessionAttributes().put("teamId", teamId);
                        System.out.println("📌 teamId 세션에 저장: " + teamId);
                    }

                } else {
                    throw new IllegalArgumentException("❌ STOMP JWT 검증 실패");
                }
            } else {
                throw new IllegalArgumentException("❌ STOMP Authorization 헤더 누락");
            }
        }

        return message;
    }
}
