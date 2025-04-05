package graduation.spokera.api.service;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.domain.match.SetScore;
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
    private final SetScoreRepository setScoreRepository;

    /**
     * 매치 조인
     */
    @Transactional
    public Boolean joinMatch(User user, Long matchId) {

        // 매칭 완료로 상태 바꿈
        Match match = matchRepository.findById(matchId).get();
        match.setStatus(MatchStatus.MATCHED);
        matchRepository.save(match);

        // 매치에 유저 들어감
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
     * 매치 생성
     */
    @Transactional
    public MatchCreateResponseDTO createMatch(MatchRequestDTO matchRequestDto, User user) {
        Match match = setMatch(matchRequestDto, user);
        matchRepository.save(match);

        log.info("새로운 매치 생성 : {}", match);

        // 경기멤버 저장
        MatchParticipant matchParticipant = setMatchParticipant(user, match, TeamType.RED);
        matchParticipantRepository.save(matchParticipant);

        MatchCreateResponseDTO matchCreateResponseDTO = new MatchCreateResponseDTO();
        matchCreateResponseDTO.setMatchId(match.getMatchId());

        return matchCreateResponseDTO;
    }

    /**
     * 특정 매치에 있는 사용자들의 위치를 기반으로 시설 추천
     */
    public List<FacilityRecommendResponseDTO> recommendFacilitiesForMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("해당 매치를 찾을 수 없습니다."));

        if (match.getStatus() != MatchStatus.MATCHED) {
            throw new RuntimeException("아직 매칭되지 않은 경기입니다.");
        }

        // 매칭에 참여한 사용자 리스트 가져오기
        List<User> users = matchParticipantRepository.findByMatch(match)
                .stream()
                .map(MatchParticipant::getUser)
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            throw new RuntimeException("매치에 참여한 사용자가 없습니다.");
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
        return match;
    }

    /**
     * 경기 추천 (TODO: 랜덤방식 -> 추천)
     */
    public List<Match> getRecommendedMatches() {
        List<Match> matches = matchRepository.findByStatus(MatchStatus.WAITING);
        matches.forEach(match ->
                match.setRecommendationScore(ThreadLocalRandom.current().nextInt(0, 11))
        );

        matches.sort(Comparator.comparing(Match::getRecommendationScore).reversed());
        return matches;
    }

    /**
     * 경기 결과 입력 및 반영
     */
    @Transactional
    public MatchResultInputResponseDTO inputMatchResult(MatchResultInputRequestDTO requestDTO) {
        Match match = matchRepository.findById(requestDTO.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        // 이미 결과 입력 완료된 매치면은 안받음
        if (match.getStatus() == MatchStatus.COMPLETED){
            MatchResultInputResponseDTO responseDTO = new MatchResultInputResponseDTO();
            responseDTO.setMatchId(requestDTO.getMatchId());
            responseDTO.setSuccess(false);
            responseDTO.setMessage("이미 결과가 입력된 매치입니다.");
            return responseDTO;
        }


        List<MatchParticipant> matchParticipantList = matchParticipantRepository.findByMatch(match);

        List<Integer> redTeamScores = requestDTO.getRedTeamScores();
        List<Integer> blueTeamScores = requestDTO.getBlueTeamScores();

        log.info("{}", redTeamScores);
        log.info("{}", blueTeamScores);
        log.info("{}", requestDTO.getWinnerTeam());

        // 경기 결과 DB 저장
        match.setStatus(MatchStatus.COMPLETED);
        match.setWinnerTeam(requestDTO.getWinnerTeam());
        matchRepository.save(match);


        // 경기 스코어 DB 저장
        for (int i = 0; i < redTeamScores.size(); i++) {

            Integer setNumber = i + 1;
            SetScore setScore = SetScore.builder()
                    .setNumber(setNumber)
                    .redTeamScore(redTeamScores.get(i))
                    .blueTeamScore(blueTeamScores.get(i))
                    .match(match)
                    .build();

            setScoreRepository.save(setScore);

        }

        // 유저 레이팅 점수 저장 (TODO: 현재는 고정으로 +10, -10, 배드민턴 점수만 올림)
        for (MatchParticipant participant : matchParticipantList) {
            User user = userRepository.findById(participant.getUser().getId()).get();
            if (requestDTO.getWinnerTeam() == participant.getTeam()) {
                user.setBadmintonRating(user.getBadmintonRating() + 10);
            } else {
                user.setBadmintonRating(user.getBadmintonRating() + -10);
            }
            userRepository.save(user);
        }


        // 성공 response DTO
        MatchResultInputResponseDTO responseDTO = new MatchResultInputResponseDTO();
        responseDTO.setMatchId(requestDTO.getMatchId());
        responseDTO.setSuccess(true);
        responseDTO.setMessage("경기 결과가 전송되었습니다.");

        return responseDTO;
    }


    public List<MatchHistoryProjectionDTO> getMatchHistory(Long userId) {
        return matchRepository.getUserMatchHistory(userId);
    }
}