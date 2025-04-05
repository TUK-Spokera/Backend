package graduation.spokera.api.controller;

import graduation.spokera.api.dto.match.*;
import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.dto.user.MatchHistoryProjectionDTO;
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
    public ResponseEntity<List<Match>> requestMatch(@RequestBody MatchRequestDTO matchRequestDto) {
        Optional<User> userOpt = userRepository.findById(matchRequestDto.getUserId());
        log.info("{}", matchRequestDto);
        if (userOpt.isEmpty()) {
            log.info("userid={} 사용자를 찾을 수 없습니다.", matchRequestDto.getUserId());
            return ResponseEntity.badRequest().build();
        }

        List<Match> matches = matchService.getRecommendedMatches();
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/create")
    public ResponseEntity<MatchCreateResponseDTO> createMatch(@RequestBody MatchRequestDTO matchRequestDto) {
        Optional<User> userOpt = userRepository.findById(matchRequestDto.getUserId());

        if (userOpt.isEmpty()) {
            log.info("userid={} 사용자를 찾을 수 없습니다.", matchRequestDto.getUserId());
            return ResponseEntity.badRequest().build();
        }


        MatchCreateResponseDTO matchCreateResponseDTO = matchService.createMatch(matchRequestDto, userOpt.get());
        return ResponseEntity.ok(matchCreateResponseDTO);
    }

    @PostMapping("/join")
    public ResponseEntity<Boolean> joinMatch(@RequestBody MatchJoinRequestDTO matchJoinRequestDTO){
        Optional<User> userOpt = userRepository.findById(matchJoinRequestDTO.getUserId());

        Boolean joinResult = matchService.joinMatch(userOpt.get(), matchJoinRequestDTO.getMatchId());
        return ResponseEntity.ok(joinResult);
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
    public List<MatchHistoryProjectionDTO> matchHistory(@PathVariable Long userId){
        List<MatchHistoryProjectionDTO> userMatchHistory = matchService.getMatchHistory(userId);
        return userMatchHistory;
    }
}