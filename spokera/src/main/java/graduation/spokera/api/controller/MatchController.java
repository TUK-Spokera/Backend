package graduation.spokera.api.controller;

import graduation.spokera.api.dto.match.*;
import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.dto.user.MatchHistoryResponseDTO;
import graduation.spokera.api.service.MatchService;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/match")
@Slf4j
public class MatchController {

    private final MatchService matchService;
    private final UserRepository userRepository;

    public MatchController(MatchService matchService, UserRepository userRepository) {
        this.matchService = matchService;
        this.userRepository = userRepository;
    }

    @PostMapping("/recommend")
    public ResponseEntity<List<Match>> requestMatch(@RequestBody MatchRecommendRequestDTO matchRecommendRequestDto) {
        Optional<User> userOpt = userRepository.findById(matchRecommendRequestDto.getUserId());
        log.info("{}", matchRecommendRequestDto);
        if (userOpt.isEmpty()) {
            log.info("userid={} 사용자를 찾을 수 없습니다.", matchRecommendRequestDto.getUserId());
            return ResponseEntity.badRequest().build();
        }

        User requestingUser = userOpt.get();
        // 요청자의 User 엔티티에 저장된 위치 정보를 활용하여 추천 진행
        List<Match> matches = matchService.getRecommendedMatches(matchRecommendRequestDto, requestingUser);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/create")
    public ResponseEntity<MatchCreateResponseDTO> createMatch(@RequestBody MatchRecommendRequestDTO matchRecommendRequestDto) {
        Optional<User> userOpt = userRepository.findById(matchRecommendRequestDto.getUserId());

        if (userOpt.isEmpty()) {
            log.info("userid={} 사용자를 찾을 수 없습니다.", matchRecommendRequestDto.getUserId());
            return ResponseEntity.badRequest().build();
        }


        MatchCreateResponseDTO matchCreateResponseDTO = matchService.createMatch(matchRecommendRequestDto, userOpt.get());
        return ResponseEntity.ok(matchCreateResponseDTO);
    }

    @PostMapping("/join")
    public MatchJoinResponseDTO joinMatch(@RequestBody MatchJoinRequestDTO matchJoinRequestDTO){

        Long userId = matchJoinRequestDTO.getUserId();
        Long matchId = matchJoinRequestDTO.getMatchId();

        return matchService.joinMatch(userId, matchId);
    }


    /**
     * 경기 승패 결과 입력
     */
    @PostMapping("/result-input")
    public MatchResultInputResponseDTO inputMatchResult(@RequestBody MatchResultInputRequestDTO matchResultInputRequestDTO){
        return matchService.inputMatchResult(matchResultInputRequestDTO);
    }

    /**
     * 대전기록 불러오기
     */
    @GetMapping("/history/{userId}")
    public List<MatchHistoryResponseDTO> matchHistory(@PathVariable Long userId){
        List<MatchHistoryResponseDTO> userMatchHistory = matchService.getMatchHistory(userId);
        return userMatchHistory;
    }
}