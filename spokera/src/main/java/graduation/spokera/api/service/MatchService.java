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
import graduation.spokera.api.dto.user.MatchHistoryResponseDTO;
import graduation.spokera.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
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
     * TODO : 2:2 이상일시 팀 배분
     */
    @Transactional
    public MatchJoinResponseDTO joinMatch(Long userId, Long matchId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없음"));
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("매치를 찾을 수 없음"));

        List<MatchParticipant> matchParticipantList = matchParticipantRepository.findByMatch(match);

        // 이미 참가한 상태면은 돌려보냄
        boolean alreadyJoiend = matchParticipantList
                .stream()
                .anyMatch(mp -> mp.getUser().getId().equals(user.getId()));

        if (alreadyJoiend){
            return MatchJoinResponseDTO.builder()
                    .success(false)
                    .message("이미 참가 한 유저입니다.")
                    .build();
        }

        // 풀방이면 못들어감
        if (matchParticipantList.size() >= match.getMatchType().getMaxParticipants())
        {
            return MatchJoinResponseDTO.builder()
                    .success(false)
                    .message("정원이 가득 찼습니다.")
                    .build();
        }


        // 매치에 유저 들어감
        MatchParticipant matchParticipant = MatchParticipant.builder()
                .match(match)
                .team(TeamType.BLUE)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();
        matchParticipantRepository.save(matchParticipant);

        // 매칭 완료로 상태 바꿈
        match.setStatus(MatchStatus.MATCHED);
        matchRepository.save(match);

        return MatchJoinResponseDTO.builder()
                .success(true)
                .message("참가가 되었습니다.")
                .build();

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

        return MatchCreateResponseDTO.builder()
                .createdMatchId(match.getMatchId())
                .success(true)
                .message("매치가 생성되었습니다.")
                .build();
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
     * 경기 추천
     */
    public List<Match> getRecommendedMatches() {
        List<Match> matches = matchRepository.findByStatus(MatchStatus.WAITING);

        // (TODO: 추천도 랜덤방식 -> 추천)
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
        if (match.getStatus() == MatchStatus.COMPLETED) {
            MatchResultInputResponseDTO responseDTO = new MatchResultInputResponseDTO();
            responseDTO.setMatchId(requestDTO.getMatchId());
            responseDTO.setSuccess(false);
            responseDTO.setMessage("이미 결과가 입력된 매치입니다.");
            return responseDTO;
        }


        List<MatchParticipant> matchParticipantList = matchParticipantRepository.findByMatch(match);

        List<Integer> redTeamScores = requestDTO.getRedTeamScores();
        List<Integer> blueTeamScores = requestDTO.getBlueTeamScores();

        // 스코어 검증
        if (redTeamScores == null || blueTeamScores == null || redTeamScores.size() != blueTeamScores.size()) {
            throw new IllegalArgumentException("세트 점수 리스트가 올바르지 않습니다.");
        }

        int redSetWins = 0;
        int blueSetWins = 0;

        for (int i = 0; i < redTeamScores.size(); i++) {
            int redScore = redTeamScores.get(i);
            int blueScore = blueTeamScores.get(i);

            // 각 세트의 점수가 동일하면 무효 (무승부가 허용되지 않는 경우)
            if (redScore == blueScore) {
                throw new IllegalArgumentException("세트 " + (i + 1) + "의 점수가 무승부입니다.");
            }

            if (redScore > blueScore) {
                redSetWins++;
            } else {
                blueSetWins++;
            }
        }

        // 예를 들어, 세트 승수가 더 많은 팀을 승리 팀으로 결정
        TeamType calculatedWinnerTeam;
        if (redSetWins > blueSetWins) {
            calculatedWinnerTeam = TeamType.RED;
        } else if (blueSetWins > redSetWins) {
            calculatedWinnerTeam = TeamType.BLUE;
        } else {
            throw new IllegalArgumentException("세트 승수가 동일하여 승리 팀을 결정할 수 없습니다.");
        }

        // 선언된 승리 팀과 계산된 승리 팀이 일치하는지 검증
        if (!calculatedWinnerTeam.equals(requestDTO.getWinnerTeam())) {
            throw new IllegalArgumentException("세트 점수와 선언된 승리 팀이 일치하지 않습니다.");
        }


        // 경기 결과 DB 저장
        match.setStatus(MatchStatus.COMPLETED);
        match.setWinnerTeam(requestDTO.getWinnerTeam());
        matchRepository.save(match);


        // 경기 스코어 DB 저장
        for (int i = 0; i < redTeamScores.size(); i++) {
            Integer setNumber = i + 1;

            SetScore setScoreResponseDTO = SetScore.builder()
                    .setNumber(setNumber)
                    .redTeamScore(redTeamScores.get(i))
                    .blueTeamScore(blueTeamScores.get(i))
                    .match(match)
                    .build();

            setScoreRepository.save(setScoreResponseDTO);
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

    /**
     * 유저 대전기록 조회
     */
    public List<MatchHistoryResponseDTO> getMatchHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없음"));
        List<MatchParticipant> joinedMatches = matchParticipantRepository.findByUser(user);

        List<MatchHistoryResponseDTO> matchHistoryList = new ArrayList<>();

        for (MatchParticipant joinedMatch : joinedMatches) {

            // Complete 된 매치가 아닌경우 continue
            if (joinedMatch.getMatch().getStatus() != MatchStatus.COMPLETED)
               continue;

            List<SetScore> setScoreList = setScoreRepository.findByMatch(joinedMatch.getMatch());
            Match match = joinedMatch.getMatch();

            // 경기결과
            MatchResult result;
            if (joinedMatch.getTeam() == match.getWinnerTeam())
                result = MatchResult.WIN;
            else
                result = MatchResult.LOSE;

            // 스코어
            List<SetScoreResponseDTO> setScoreResponseDTOList = setScoreList.stream()
                    .map(SetScoreResponseDTO::toDTO)
                    .toList();

            // 응답 생성
            MatchHistoryResponseDTO matchHistoryResponseDTO = MatchHistoryResponseDTO.builder()
                    .matchId(match.getMatchId())
                    .sportType(match.getSportType())
                    .matchType(match.getMatchType())
                    .setScores(setScoreResponseDTOList)
                    .startTime(match.getStartTime())
                    .endTime(match.getEndTime())
                    .teamType(joinedMatch.getTeam())
                    .result(result)
                    .build();

            matchHistoryList.add(matchHistoryResponseDTO);
        }

        return matchHistoryList;
    }

}