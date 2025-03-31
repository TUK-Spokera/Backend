package graduation.spokera.api.service;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.domain.type.MatchResult;
import graduation.spokera.api.domain.type.MatchType;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.dto.facility.FacilityRecommendResponseDTO;
import graduation.spokera.api.dto.match.*;
import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.domain.type.TeamType;
import graduation.spokera.api.domain.user.UserRepository;
import graduation.spokera.api.dto.user.MatchHistoryProjectionDTO;
import graduation.spokera.api.dto.user.UserSubmissionInfoDTO;
import graduation.spokera.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final FacilityService facilityService;
    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;

    /**
     * 매칭방에 들어가기
     */
    @Transactional
    public Boolean joinMatch(User user, Long matchId) {

        // 매칭 완료로 상태 바꿈
        Match match = matchRepository.findById(matchId).get();
        match.setStatus(MatchStatus.MATCHED);
        matchRepository.save(match);

        // 매칭에 유저 들어감
        MatchParticipant matchParticipant = MatchParticipant.builder()
                .match(match)
                .team(TeamType.BLUE)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();
        matchParticipantRepository.save(matchParticipant);

        return true;

    }

    /**
     * 매칭방 생성
     */
    @Transactional
    public MatchCreateResponseDTO createMatch(MatchRequestDTO matchRequestDto, User user) {
        Match match = setMatch(matchRequestDto, user);
        matchRepository.save(match);

        log.info("새로운 매칭방 생성 : {}", match);

        // 경기멤버 저장
        MatchParticipant matchParticipant = setMatchParticipant(user, match, TeamType.RED);
        matchParticipantRepository.save(matchParticipant);

        MatchCreateResponseDTO matchCreateResponseDTO = new MatchCreateResponseDTO();
        matchCreateResponseDTO.setMatchId(match.getMatchId());

        return matchCreateResponseDTO;
    }

    /**
     * 특정 매칭방에 있는 사용자들의 위치를 기반으로 시설 추천
     */
    public List<FacilityRecommendResponseDTO> recommendFacilitiesForMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("해당 매칭방을 찾을 수 없습니다."));

        if (match.getStatus() != MatchStatus.MATCHED) {
            throw new RuntimeException("아직 매칭되지 않은 경기입니다.");
        }

        // 매칭에 참여한 사용자 리스트 가져오기
        List<User> users = matchParticipantRepository.findByMatch(match)
                .stream()
                .map(MatchParticipant::getUser)
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            throw new RuntimeException("매칭방에 참여한 사용자가 없습니다.");
        }

        // 활용하여 시설 추천
        return facilityService.recommendFacilities(users, match.getSportType(), 5);
    }

    private static MatchParticipant setMatchParticipant(User user, Match match, TeamType teamType) {
        MatchParticipant matchParticipant = new MatchParticipant();
        matchParticipant.setUser(user);
        matchParticipant.setTeam(teamType);
        matchParticipant.setMatch(match);
        matchParticipant.setJoinedAt(LocalDateTime.now());
        return matchParticipant;
    }

    private static Match setMatch(MatchRequestDTO matchRequestDto, User user) {
        Match match = new Match();
        match.setSportType(matchRequestDto.getSportType());
        match.setStartTime(matchRequestDto.getStartTime());
        match.setEndTime(matchRequestDto.getEndTime());
        match.setMatchType(matchRequestDto.getMatchType());
        match.setStatus(MatchStatus.WAITING);
//        match.setRecommendationScore(ThreadLocalRandom.current().nextInt(0, 11));
        return match;
    }

    public List<Match> getWaitingMatches() {
//        return matchRepository.findByStatus(MatchStatus.WAITING);
        return matchRepository.findByStatus(MatchStatus.WAITING);
    }

    public List<Match> getRecommendedMatches() {
        List<Match> matches = matchRepository.findByStatus(MatchStatus.WAITING);
        matches.forEach(match ->
                match.setRecommendationScore(ThreadLocalRandom.current().nextInt(0, 11))
        );

        matches.sort(Comparator.comparing(Match::getRecommendationScore).reversed());
        return matches;
    }

    /**
     * 경기 결과 입력
     */
    public MatchResultInputResponseDTO inputMatchResult(MatchResultInputRequestDTO requestDTO) {
        MatchResultInputResponseDTO responseDTO = new MatchResultInputResponseDTO();
        responseDTO.setMatchId(requestDTO.getMatchId());
        Match match = matchRepository.findById(requestDTO.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        MatchParticipant participant = matchParticipantRepository.findByMatchAndUser(match, user)
                .orElseThrow(() -> new RuntimeException("User(participant) not found"));

        UserSubmissionInfoDTO userSubmissionInfoDTO = new UserSubmissionInfoDTO(user.getId(), user.getNickname(), user.getRating(), participant.getTeam());

        // 새로운 제출 정보를 생성하여 저장
        MatchSubmissionDTO submission = MatchSubmissionDTO.builder()
                .user(userSubmissionInfoDTO)
                .matchResult(requestDTO.getMatchResult())
                .build();

        MatchSubmissionMemoryStore.addSubmission(requestDTO.getMatchId(), submission);

        // 해당 매치의 제출 내역 리턴
        return getInputResponseDTO(match);
    }

    /**
     * 경기 승패 제출 내역 반환
     */
    public MatchResultInputResponseDTO getMatchResultStatus(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        return getInputResponseDTO(match);
    }

    /**
     * 승패 제출 내역 반환해주는 메소드
     * (모두 제출하면 승/패에 따라서 점수를 부여하고, 매치테이블에 이긴 팀 기록)
     */
    private MatchResultInputResponseDTO getInputResponseDTO(Match match) {
        Long matchId = match.getMatchId();
        MatchResultInputResponseDTO matchResultInputResponseDTO = new MatchResultInputResponseDTO();
        List<MatchSubmissionDTO> submissions = MatchSubmissionMemoryStore.getSubmissions(matchId);
        matchResultInputResponseDTO.setMatchId(matchId);
        matchResultInputResponseDTO.setSubmissions(submissions);

        // 승패 전부 제출 여부 체크 (예: ONE_VS_ONE이면 2건, TWO_VS_TWO이면 4건 이상이면 완료)
        int submissionCount = submissions.size();
        if ((match.getMatchType() == MatchType.ONE_VS_ONE && submissionCount >= 2) ||
                (match.getMatchType() == MatchType.TWO_VS_TWO && submissionCount >= 4)) {
            matchResultInputResponseDTO.setMatchCompleted(true);

            updateUserRatingAndSaveWinnerTeam(match, matchResultInputResponseDTO);

        } else {
            matchResultInputResponseDTO.setMatchCompleted(false);
        }

        return matchResultInputResponseDTO;
    }

    /**
     * 승패에 따른 점수 부여, 승리 팀 기록
     * TODO 점수부여방식 고정 => 점수차에따라 바꾸기
     */
    private void updateUserRatingAndSaveWinnerTeam(Match match, MatchResultInputResponseDTO matchResultInputResponseDTO) {

        int redTeamWinCount = 0, blueTeamWinCount = 0;
        TeamType winnerTeam;
        List<MatchSubmissionDTO> submissions = matchResultInputResponseDTO.getSubmissions();

        for (MatchSubmissionDTO submission : submissions) {
            TeamType team = submission.getUser().getTeam();
            MatchResult result = submission.getMatchResult();

            if ((team == TeamType.BLUE && result == MatchResult.WIN) || (team == TeamType.RED && result == MatchResult.LOSE)) {
                blueTeamWinCount++;
            } else {
                redTeamWinCount++;
            }
        }

        if (blueTeamWinCount >= redTeamWinCount) {
            winnerTeam = TeamType.BLUE;
        } else {
            winnerTeam = TeamType.RED;
        }

        // 승리팀 기록
        match.setWinnerTeam(winnerTeam);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);

        // 개인 점수 기록
        for (MatchSubmissionDTO submission : submissions) {
            User user = userRepository.findById(submission.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("user not found"));

            if (submission.getUser().getTeam() == winnerTeam) {
                user.setRating(user.getRating() + 10);
            }
            else {
                user.setRating(user.getRating() - 10);
            }

            userRepository.save(user);
        }
    }


    public List<MatchHistoryProjectionDTO> getMatchHistory(Long userId) {
        return matchRepository.getUserMatchHistory(userId);
    }
}