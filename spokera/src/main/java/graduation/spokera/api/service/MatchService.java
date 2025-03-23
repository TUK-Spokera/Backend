package graduation.spokera.api.service;

import graduation.spokera.api.domain.facility.Facility;
import graduation.spokera.api.domain.facility.FacilityVote;
import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.dto.facility.FacilityRecommendResponseDTO;
import graduation.spokera.api.dto.facility.FacilityVoteRequestDTO;
import graduation.spokera.api.dto.facility.FacilityVoteResponseDTO;
import graduation.spokera.api.dto.match.MatchCreateResponseDTO;
import graduation.spokera.api.dto.match.MatchRequestDTO;
import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.domain.type.TeamType;
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
    private final FacilityVoteRepository facilityVoteRepository;

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
     * 경기장 투표
     */
    public FacilityVoteResponseDTO voteFacility(FacilityVoteRequestDTO facilityVoteRequestDTO){

        Facility facility = facilityRepository.findByFaciId(facilityVoteRequestDTO.getFacilityId())
                .orElseThrow(() -> new RuntimeException("시설 없음"));

        User user = userRepository.findById(facilityVoteRequestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        Match match = matchRepository.findById(facilityVoteRequestDTO.getMatchId())
                .orElseThrow(() -> new RuntimeException("매칭 없음"));

        FacilityVote facilityVote = FacilityVote.builder()
                .facility(facility)
                .user(user)
                .match(match)
                .build();

        facilityVoteRepository.save(facilityVote);

        FacilityVoteResponseDTO facilityVoteResponseDTO = FacilityVoteResponseDTO.builder()
                .success(true)
                .message("투표 성공")
                .facilityVoteRequestDTO(facilityVoteRequestDTO)
                .build();

        return facilityVoteResponseDTO;

    }
}