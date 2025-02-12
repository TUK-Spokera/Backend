package graduation.spokera.api.service;

import graduation.spokera.api.model.*;
import graduation.spokera.api.dto.MatchRequestDTO;
import graduation.spokera.api.dto.MatchResponseDTO;
import graduation.spokera.api.model.enums.MatchStatus;
import graduation.spokera.api.model.enums.TeamType;
import graduation.spokera.api.repository.ChatRoomRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final FacilityService facilityService;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public MatchResponseDTO findOrCreateMatch(MatchRequestDTO matchRequestDto, User user) {
        // 시간대 되는 매칭 찾기
        Optional<Match> availableMatch = matchRepository.findAvailableMatch(
                matchRequestDto.getSportType(),
                matchRequestDto.getStartTime(),
                matchRequestDto.getEndTime(),
                matchRequestDto.getMatchType()
        );

        // 가능한 매칭이 있다면 매칭이 잡히고 경기장 추천하고 채팅방 번호랑 같이 리턴
        if (availableMatch.isPresent()) {
            Match match = availableMatch.get();

            // 매칭된 유저 리스트 가져오기
            List<User> matchedUsers = matchRepository.findMatchedUsers(match.getSportType(), match.getMatchType());

            // 경기장 추천
            List<Facility> facilityList = facilityService.recommendFacilities(matchedUsers, matchRequestDto.getSportType(), 5);

            // 응답 DTO 작성
            MatchResponseDTO matchResponseDTO = getMatchSuccessResponseDTO(match, facilityList);

            // 경기멤버 데이터베이스에 저장
            MatchParticipant matchParticipant = setMatchParticipant(user, match);
            matchParticipantRepository.save(matchParticipant);

            // 데이터베이스에 매칭됨 저장

            return matchResponseDTO;
        }


        // 새로운 매칭 생성
        Match newMatch = createMatch(matchRequestDto, user);

        log.info("새로운 매칭방 생성 : {}", newMatch);
        matchRepository.save(newMatch);

        ChatRoom chatRoom = chatRoomService.createChatRoom("Match_" + newMatch.getMatchId());
        chatRoomRepository.save(chatRoom);
        matchRepository.updateChatroom(newMatch.getMatchId(), chatRoom);

        MatchParticipant matchParticipant = setMatchParticipant(user, newMatch);
        matchParticipantRepository.save(matchParticipant);

        MatchResponseDTO matchResponseDTO = new MatchResponseDTO();
        matchResponseDTO.setStatus(MatchStatus.WAITING);
        matchResponseDTO.setChatRoomName(chatRoom.getName());

        return matchResponseDTO;
    }

    private static MatchParticipant setMatchParticipant(User user, Match match) {
        MatchParticipant matchParticipant = new MatchParticipant();
        matchParticipant.setUser(user);
        matchParticipant.setTeam(TeamType.RED);
        matchParticipant.setMatch(match);
        matchParticipant.setJoinedAt(LocalDateTime.now());
        return matchParticipant;
    }

    private static Match createMatch(MatchRequestDTO matchRequestDto, User user) {
        Match match = new Match();
        match.setUser(user);
        match.setSportType(matchRequestDto.getSportType());
        match.setStartTime(matchRequestDto.getStartTime());
        match.setEndTime(matchRequestDto.getEndTime());
        match.setMatchType(matchRequestDto.getMatchType());
        return match;
    }

    private static MatchResponseDTO getMatchSuccessResponseDTO(Match match, List<Facility> facilityList) {
        MatchResponseDTO matchResponseDTO = new MatchResponseDTO();
        matchResponseDTO.setMatchId(match.getMatchId());
        matchResponseDTO.setSportType(match.getSportType());
        matchResponseDTO.setStartTime(match.getStartTime());
        matchResponseDTO.setMatchType(match.getMatchType());
        matchResponseDTO.setChatRoomName(match.getChatRoom().getName());
        matchResponseDTO.setFacilityList(facilityList);
        matchResponseDTO.setStatus(MatchStatus.MATCHED);
        return matchResponseDTO;
    }

    public List<Match> getWaitingMatches() {
        return matchRepository.findByStatus(MatchStatus.WAITING);
    }
}