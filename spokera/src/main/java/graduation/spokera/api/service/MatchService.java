package graduation.spokera.api.service;

import graduation.spokera.api.model.*;
import graduation.spokera.api.dto.MatchRequestDTO;
import graduation.spokera.api.dto.MatchResponseDTO;
import graduation.spokera.api.model.enums.MatchStatus;
import graduation.spokera.api.model.enums.TeamType;
import graduation.spokera.api.repository.UserRepository;
import graduation.spokera.api.repository.MatchParticipantRepository;
import graduation.spokera.api.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final FacilityService facilityService;
    private final UserRepository userRepository;

    @Transactional
    public MatchResponseDTO findOrCreateMatch(MatchRequestDTO matchRequestDto, User user) {

//        // 사용자가 이미 해당 타입의 매칭에 속해 있는지 확인
//        boolean isAlreadyInMatch = matchParticipantRepository.existsByUserAndMatch_SportTypeAndMatch_MatchTypeAndMatch_Status(
//                user, matchRequestDto.getSportType(), matchRequestDto.getMatchType(), MatchStatus.WAITING
//        );
//
//        if (isAlreadyInMatch) {
//            throw new RuntimeException("이미 대기 중인 매칭에 참가하고 있습니다.");
//        }

        // 가능한 매칭 찾기
        Optional<Match> availableMatch = matchRepository.findAvailableMatch(
                matchRequestDto.getSportType(),
                matchRequestDto.getStartTime(),
                matchRequestDto.getEndTime(),
                matchRequestDto.getMatchType(),
                MatchStatus.WAITING
        );

        // 가능한 매칭이 있다면 매칭이 잡히고 경기장 추천하고 채팅방 번호랑 같이 리턴
        if (availableMatch.isPresent()) {
            Match match = availableMatch.get();

            // 매칭됨으로 status 변경
            match.setStatus(MatchStatus.MATCHED);
            matchRepository.save(match);

            // 매칭된 유저 리스트 가져오기
            List<User> matchedUsers = matchRepository.findMatchedUsers(match.getSportType(), match.getMatchType());

            // 경기장 추천
            List<Facility> facilityList = facilityService.recommendFacilities(matchedUsers, matchRequestDto.getSportType(), 5);

            // 응답 DTO 작성
            MatchResponseDTO matchResponseDTO = getMatchSuccessResponseDTO(match);

            // 경기멤버 데이터베이스에 저장
            MatchParticipant matchParticipant = setMatchParticipant(user, match, TeamType.BLUE);
            matchParticipantRepository.save(matchParticipant);

            return matchResponseDTO;
        }


        // 가능한 매칭이 없다면 새로운 매칭 생성
        Match match = createMatch(matchRequestDto, user);
        matchRepository.save(match);

        log.info("새로운 매칭방 생성 : {}", match);

        // 경기멤버 저장
        MatchParticipant matchParticipant = setMatchParticipant(user, match, TeamType.RED);
        matchParticipantRepository.save(matchParticipant);

        MatchResponseDTO matchResponseDTO = new MatchResponseDTO();
        matchResponseDTO.setMatchId(match.getMatchId());
        matchResponseDTO.setStatus(MatchStatus.WAITING);

        return matchResponseDTO;
    }

    /**
     * 특정 매칭방에 있는 사용자들의 위치를 기반으로 시설 추천
     */
    public List<Facility> recommendFacilitiesForMatch(Long matchId) {
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

        // FacilityService를 활용하여 시설 추천
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

    private static Match createMatch(MatchRequestDTO matchRequestDto, User user) {
        Match match = new Match();
        match.setSportType(matchRequestDto.getSportType());
        match.setStartTime(matchRequestDto.getStartTime());
        match.setEndTime(matchRequestDto.getEndTime());
        match.setMatchType(matchRequestDto.getMatchType());
        match.setStatus(MatchStatus.WAITING);
        return match;
    }

    private static MatchResponseDTO getMatchSuccessResponseDTO(Match match) {
        MatchResponseDTO matchResponseDTO = new MatchResponseDTO();
        matchResponseDTO.setMatchId(match.getMatchId());
        matchResponseDTO.setStatus(match.getStatus());
        return matchResponseDTO;
    }

    public List<Match> getWaitingMatches() {
        return matchRepository.findByStatus(MatchStatus.WAITING);
    }
}