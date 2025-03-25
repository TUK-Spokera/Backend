package graduation.spokera.api.service;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.user.UserRepository;
import graduation.spokera.api.dto.user.KakaoUserResponse;
import graduation.spokera.api.dto.user.TokenResponse;
import graduation.spokera.api.dto.user.UserInfoResponse;
import graduation.spokera.api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
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

    @Value("${spring.social.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.social.kakao.redirect}")
    private String kakaoRedirectUri;

    @Value("${spring.social.kakao.url.token}")
    private String kakaoTokenUrl;

    @Value("${spring.social.kakao.url.profile}")
    private String kakaoProfileUrl;

    public TokenResponse kakaoLogin(String code) {
        System.out.println("✅ [카카오 로그인] 인가 코드 수신: " + code);

        Map<String, Object> tokenMap = getKakaoTokenMap(code);
        if (tokenMap == null) return null;

        String kakaoAccessToken = (String) tokenMap.get("access_token");
        if (kakaoAccessToken == null) return null;

        // 1. 카카오 사용자 정보 조회
        KakaoUserResponse kakaoUser = getKakaoUserInfo(kakaoAccessToken);
        if (kakaoUser == null) return null;

        // 2. 유저 저장
        User user = saveOrUpdateUser(kakaoUser);
        System.out.println("✅ [카카오 로그인] 사용자 저장 완료: " + user.getNickname());

        // 3. ✅ 서버 JWT 발급
        TokenResponse jwtToken = jwtUtil.generateTokens(user);
        System.out.println("✅ [서버 JWT 발급] Access: " + jwtToken.getAccessToken());

        return jwtToken;
    }


    private Map<String, Object> getKakaoTokenMap(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoBaseUrl + kakaoRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(kakaoTokenUrl, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.out.println("❌ [카카오 로그인] 액세스 토큰 요청 실패: " + response.getStatusCode());
                System.out.println("❌ 응답 바디: " + response.getBody());
                return null;
            }

            System.out.println("✅ [카카오 로그인] 액세스 토큰 발급 성공");
            System.out.println("🔎 토큰 응답 전체: " + response.getBody());

            return response.getBody();
        } catch (RestClientException e) {
            System.out.println("❌ [카카오 로그인] RestTemplate 예외 발생: " + e.getMessage());
            return null;
        }
    }

    private KakaoUserResponse getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(kakaoProfileUrl, HttpMethod.GET, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.out.println("❌ [카카오 로그인] 사용자 정보 요청 실패: " + response.getStatusCode());
                return null;
            }

            System.out.println("✅ [카카오 로그인] 카카오 사용자 정보 응답: " + response.getBody());

            Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");
            Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;

            if (kakaoAccount == null || profile == null) {
                System.out.println("❌ [카카오 로그인] 사용자 계정 정보 또는 프로필 정보가 없음");
                return null;
            }

            KakaoUserResponse userResponse = new KakaoUserResponse();
            userResponse.setId(Long.valueOf(response.getBody().get("id").toString()));
            userResponse.setNickname(profile.get("nickname") != null ? profile.get("nickname").toString() : "Unknown");
            userResponse.setEmail(kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString() : "no-email");

            System.out.println("✅ [카카오 로그인] 사용자 정보 조회 완료: " + userResponse.getNickname() + " / " + userResponse.getEmail());
            return userResponse;
        } catch (RestClientException e) {
            System.out.println("❌ [카카오 로그인] 사용자 정보 조회 RestTemplate 예외: " + e.getMessage());
            return null;
        }
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
        System.out.println("🔁 [리프레시 토큰 요청] refreshToken: " + refreshToken);

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("❌ 유효하지 않은 JWT 형식의 리프레시 토큰입니다.");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("❌ 이 토큰은 리프레시 토큰이 아닙니다.");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        System.out.println("🔎 리프레시 토큰에서 유저 ID 추출: " + userId);

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("❌ 유저를 찾을 수 없습니다."));

        String newAccessToken = jwtUtil.generateAccessToken(user);
        System.out.println("✅ [토큰 재발급] 새로운 Access Token: " + newAccessToken);
        return new TokenResponse(newAccessToken, refreshToken);
    }

    public UserInfoResponse getUserInfoById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return new UserInfoResponse(user.getId(), user.getEmail(), user.getNickname());
    }


}
