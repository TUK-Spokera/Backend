package graduation.spokera.api.service;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.dto.user.KakaoUserResponse;
import graduation.spokera.api.dto.user.TokenResponse;
import graduation.spokera.api.repository.UserRepository;
import graduation.spokera.api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${spring.social.kakao.base-url}")
    private String kakaoBaseUrl;

    @Value("${spring.social.kakao.client-id}") // ✅ spring 유지
    private String kakaoClientId;

    @Value("${spring.social.kakao.redirect}")
    private String kakaoRedirectUri;

    @Value("${spring.social.kakao.url.token}")
    private String kakaoTokenUrl;

    @Value("${spring.social.kakao.url.profile}")
    private String kakaoProfileUrl;

    public TokenResponse kakaoLogin(String code) {
        System.out.println("✅ [카카오 로그인] 인가 코드 수신: " + code);

        String kakaoAccessToken = getKakaoAccessToken(code);
        if (kakaoAccessToken == null) {
            System.out.println("❌ [카카오 로그인] 액세스 토큰 발급 실패");
            return null;
        }

        KakaoUserResponse kakaoUser = getKakaoUserInfo(kakaoAccessToken);
        if (kakaoUser == null) {
            System.out.println("❌ [카카오 로그인] 사용자 정보 조회 실패");
            return null;
        }

        User user = saveOrUpdateUser(kakaoUser);
        System.out.println("✅ [카카오 로그인] 사용자 저장 완료: " + user.getNickname() + " (" + user.getEmail() + ")");

        TokenResponse tokenResponse = jwtUtil.generateTokens(user);
        System.out.println("✅ [카카오 로그인] JWT 발급 완료 - Access Token: " + tokenResponse.getAccessToken());

        return tokenResponse;
    }

    private String getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri",  kakaoBaseUrl + kakaoRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(kakaoTokenUrl, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            System.out.println("❌ [카카오 로그인] 액세스 토큰 요청 실패: " + response.getStatusCode());
            return null;
        }

        System.out.println("✅ [카카오 로그인] 액세스 토큰 발급 성공");
        return response.getBody().get("access_token").toString();
    }

    private KakaoUserResponse getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(kakaoProfileUrl, HttpMethod.GET, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            System.out.println("❌ [카카오 로그인] 사용자 정보 요청 실패: " + response.getStatusCode());
            return null;
        }

        System.out.println("✅ [카카오 로그인] 카카오 사용자 정보 응답: " + response.getBody());

        // ✅ 응답 데이터를 확인 후, `null` 체크하여 예외 방지
        Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");
        if (kakaoAccount == null) {
            System.out.println("❌ [카카오 로그인] `kakao_account` 정보가 없음");
            return null;
        }

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            System.out.println("❌ [카카오 로그인] `profile` 정보가 없음");
            return null;
        }

        KakaoUserResponse userResponse = new KakaoUserResponse();
        userResponse.setId(Long.valueOf(response.getBody().get("id").toString()));
        userResponse.setNickname(profile.get("nickname") != null ? profile.get("nickname").toString() : "Unknown");
        userResponse.setEmail(kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString() : "no-email");

        System.out.println("✅ [카카오 로그인] 사용자 정보 조회 완료: " + userResponse.getNickname() + " / " + userResponse.getEmail());
        return userResponse;
    }


    private User saveOrUpdateUser(KakaoUserResponse kakaoUser) {
        return userRepository.findByKakaoId(String.valueOf(kakaoUser.getId()))
                .map(user -> {
                    user.setNickname(kakaoUser.getNickname());
                    user.setEmail(kakaoUser.getEmail());
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .kakaoId(String.valueOf(kakaoUser.getId()))
                            .nickname(kakaoUser.getNickname())
                            .email(kakaoUser.getEmail())
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        String newAccessToken = jwtUtil.generateAccessToken(user);
        return new TokenResponse(newAccessToken, refreshToken); // 리프레시 토큰은 재사용
    }

}
