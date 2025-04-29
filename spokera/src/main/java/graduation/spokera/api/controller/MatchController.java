package graduation.spokera.api.controller;

import graduation.spokera.api.dto.match.*;
import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.dto.user.MatchHistoryResponseDTO;
import graduation.spokera.api.service.AuthService;
import graduation.spokera.api.service.MatchService;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.user.UserRepository;
import graduation.spokera.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/match")
@Slf4j
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final UserRepository userRepository;

    @PostMapping("/recommend")
    public ResponseEntity<List<Match>> requestMatch(@RequestBody MatchRecommendRequestDTO matchRecommendRequestDto,
                                                    @AuthenticationPrincipal User user) {
        // 요청자의 User 엔티티에 저장된 위치 정보를 활용하여 추천 진행
        List<Match> matches = matchService.getRecommendedMatches(matchRecommendRequestDto, user);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/create")
    public ResponseEntity<MatchCreateResponseDTO> createMatch(@RequestBody MatchRecommendRequestDTO matchRecommendRequestDto,
                                                              @AuthenticationPrincipal User user) {

        MatchCreateResponseDTO matchCreateResponseDTO = matchService.createMatch(matchRecommendRequestDto, user);
        return ResponseEntity.ok(matchCreateResponseDTO);
    }

    @PostMapping("/join")
    public MatchJoinResponseDTO joinMatch(@RequestBody MatchJoinRequestDTO matchJoinRequestDTO,
                                          @AuthenticationPrincipal User user) {

        Long userId = null;
        if (matchJoinRequestDTO.getUserId() != null){
            userId = matchJoinRequestDTO.getUserId();
        }
        else{
            userId = user.getId();
        }
        Long matchId = matchJoinRequestDTO.getMatchId();

        return matchService.joinMatch(userId, matchId);
    }


    /**
     * 경기 승패 결과 입력
     */
    @PostMapping("/result-input")
    public MatchResultInputResponseDTO inputMatchResult(@RequestBody MatchResultInputRequestDTO matchResultInputRequestDTO) {
        return matchService.inputMatchResult(matchResultInputRequestDTO);
    }

    /**
     * 대전기록 불러오기
     */
    @GetMapping({"/history", "/history/{userId}"})
    public List<MatchHistoryResponseDTO> matchHistory(@AuthenticationPrincipal User user) {
        List<MatchHistoryResponseDTO> userMatchHistory = matchService.getMatchHistory(user.getId());
        return userMatchHistory;
    }

}