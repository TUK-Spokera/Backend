package graduation.spokera.api.util;

import graduation.spokera.api.repository.MatchRepository;
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
    private final MatchRepository matchRepository;

    public StompAuthChannelInterceptor(JwtUtil jwtUtil, UserRepository userRepository, MatchRepository matchRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            String matchId = accessor.getFirstNativeHeader("matchId");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                if (jwtUtil.validateToken(token)) {
                    String userId = jwtUtil.getUserIdFromToken(token);

                    User user = userRepository.findById(Long.parseLong(userId))
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    // 🔒 matchId 유효성 체크
                    if (matchId != null && !matchId.isBlank()) {
                        boolean exists = matchRepository.existsById(Long.parseLong(matchId));
                        if (!exists) {
                            throw new IllegalArgumentException("❌ 존재하지 않는 matchId: " + matchId);
                        }

                        accessor.getSessionAttributes().put("matchId", matchId);
                        System.out.println("📌 matchId 세션에 저장: " + matchId);
                    }

                    accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, List.of()));
                    System.out.println("✅ STOMP 인증 통과: " + user.getId());

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
