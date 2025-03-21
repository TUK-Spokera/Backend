package graduation.spokera.api.controller;

import graduation.spokera.api.dto.user.TokenResponse;
import  graduation.spokera.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/oauth/kakao/redirect")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestParam("code") String code) {
        TokenResponse tokenResponse = authService.kakaoLogin(code);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        String refreshToken = refreshTokenHeader.replace("Bearer ", "");
        TokenResponse newTokens = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(newTokens);
    }
}

