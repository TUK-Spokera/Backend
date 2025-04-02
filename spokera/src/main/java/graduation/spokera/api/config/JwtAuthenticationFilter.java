//package graduation.spokera.api.config;
//
//import graduation.spokera.api.util.JwtUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter extends OncePerRequestFilter { // ✅ OncePerRequestFilter 사용
//
//    private final JwtUtil jwtUtil;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        // ✅ 요청 헤더에서 JWT 토큰 추출
//        String token = resolveToken(request);
//
//        // ✅ 토큰이 유효한 경우, 사용자 정보를 SecurityContext에 저장
//        if (token != null && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
//            String userId = jwtUtil.getUserIdFromToken(token);
//
//            // ✅ Spring Security에서 사용할 UserDetails 객체 생성
//            UserDetails userDetails = User.withUsername(userId)
//                    .password("") // JWT 인증이므로 비밀번호는 필요 없음
//                    .roles("USER") // 기본적으로 USER 역할 부여
//                    .build();
//
//            // ✅ Authentication 객체 생성
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
//            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//            // ✅ SecurityContextHolder에 인증 정보 저장
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        // ✅ 다음 필터로 요청 전달
//        filterChain.doFilter(request, response);
//    }
//
//    // ✅ HTTP 요청에서 Bearer 토큰 추출
//    private String resolveToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
//}
package graduation.spokera.api.config;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.user.UserRepository;
import graduation.spokera.api.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ✅ 요청 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        // ✅ 토큰 유효성 검사
        if (token != null && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
            String userId = jwtUtil.getUserIdFromToken(token);

            // ✅ 실제 User 엔티티 조회
            User user = userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ SecurityContext에 인증 객체 저장
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, token, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // ✅ Bearer 토큰 파싱
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
