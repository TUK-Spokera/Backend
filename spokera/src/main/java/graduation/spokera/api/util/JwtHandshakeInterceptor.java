package graduation.spokera.api.util;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.*;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String path = request.getURI().getPath();
        System.out.println("🔌 [Handshake 시도] 요청 경로: " + path);

        if (path.contains("/info")) {
            System.out.println("ℹ️ SockJS info 요청 허용됨");
            return true;
        }

        List<String> authHeaders = request.getHeaders().get("Authorization");
        List<String> teamHeaders = request.getHeaders().get("teamId");

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String token = authHeaders.get(0).replace("Bearer ", "");

            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.getUserIdFromToken(token);
                attributes.put("userId", userId);

                if (teamHeaders != null && !teamHeaders.isEmpty()) {
                    attributes.put("teamId", teamHeaders.get(0));
                }

                System.out.println("✅ [Handshake 통과] userId: " + userId);
                return true;
            } else {
                System.out.println("❌ JWT 검증 실패");
            }
        } else {
            System.out.println("❌ Authorization 헤더 없음");
        }

        return false;
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
