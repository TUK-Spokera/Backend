package graduation.spokera.api.controller;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.dto.user.TokenResponse;
import graduation.spokera.api.dto.user.UserInfoResponse;
import  graduation.spokera.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/oauth/kakao/redirect")
    public ResponseEntity<Void> kakaoLogin(@RequestParam("code") String code) {
        TokenResponse tokenResponse = authService.kakaoLogin(code);

        String redirectUri = String.format(
                "spokera://login/callback?accessToken=%s&refreshToken=%s",
                URLEncoder.encode(tokenResponse.getAccessToken(), StandardCharsets.UTF_8),
                URLEncoder.encode(tokenResponse.getRefreshToken(), StandardCharsets.UTF_8)
        );

        return ResponseEntity.status(302)  // 👈 302로 리다이렉트
                .location(URI.create(redirectUri))
                .build();
    }



    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        String refreshToken = refreshTokenHeader.replace("Bearer ", "");
        TokenResponse newTokens = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(newTokens);
    }

    @GetMapping("/user/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfoResponse userInfo = authService.getUserInfoById(user.getId());
        return ResponseEntity.ok(userInfo);
    }



}

