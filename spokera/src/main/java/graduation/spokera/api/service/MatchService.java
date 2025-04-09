package graduation.spokera.api.service;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.domain.match.SetScore;
import graduation.spokera.api.domain.type.MatchResult;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

        if (alreadyJoiend) {
            return MatchJoinResponseDTO.builder()
                    .success(false)
                    .message("이미 참가 한 유저입니다.")
                    .build();
        }

        // 풀방이면 못들어감
        if (matchParticipantList.size() >= match.getMatchType().getMaxParticipants()) {
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
    public MatchCreateResponseDTO createMatch(MatchRecommendRequestDTO matchRecommendRequestDto, User user) {

        Match match = Match.builder()
                .sportType(matchRecommendRequestDto.getSportType())
                .startTime(matchRecommendRequestDto.getStartTime())
                .endTime(matchRecommendRequestDto.getEndTime())
                .matchType(matchRecommendRequestDto.getMatchType())
                .status(MatchStatus.WAITING)
                .build();

        matchRepository.save(match);

        log.info("새로운 매치 생성 : {}", match);

        // 경기멤버 저장
        MatchParticipant matchParticipant = MatchParticipant.builder()
                .user(user)
                .team(TeamType.RED)
                .match(match)
                .joinedAt(LocalDateTime.now())
                .build();


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


    /**
     * 매치 추천 - 클라이언트가 보낸 DTO 기반
     * 요청자의 User 엔티티 속 위치(latitude, longitude)를 활용해서,
     * 매치 내 이미 참여한 유저들과의 위치 차이를 계산합니다.
     */
    public List<Match> getRecommendedMatches(MatchRecommendRequestDTO requestDTO, User requestingUser) {
        // 1. 모든 대기 중인 매치 조회
        List<Match> matches = matchRepository.findByStatus(MatchStatus.WAITING);

        if (matches.isEmpty()) return List.of();

        // 2. 매치 ID 리스트로 모든 참가자 한번에 조회
        List<MatchParticipant> allParticipants = matchParticipantRepository.findByMatchIn(matches);

        // 3. 매치 ID별로 참가자 맵 구성
        Map<Long, List<MatchParticipant>> matchParticipantsMap = allParticipants.stream()
                .collect(Collectors.groupingBy(mp -> mp.getMatch().getMatchId()));

        // 4. 요청자가 참가하지 않은 매치만 필터링
        matches = matches.stream()
                .filter(match -> {
                    List<MatchParticipant> participants = matchParticipantsMap.get(match.getMatchId());
                    if (participants == null) return true;
                    return participants.stream()
                            .noneMatch(mp -> mp.getUser().getId().equals(requestingUser.getId()));
                })
                .collect(Collectors.toList());


        // 클라이언트 요청 정보
        String desiredSport = requestDTO.getSportType();
        LocalDateTime desiredTime = requestDTO.getStartTime();
        // 요청자의 위치 정보
        double userLat = requestingUser.getLatitude();
        double userLon = requestingUser.getLongitude();

        // 각 매치별 점수 계산
        for (Match match : matches) {
            // 1. 위치 점수: 요청자와 매치에 참여한 유저들의 평균 거리를 기준으로 점수 산출
            double locationScore = calculateLocationScoreForMatch(userLat, userLon, match);
            // 2. 시간 점수: 원하는 시작 시간과 매치 시작 시간의 차이를 기반으로 산출
            double timeScore = calculateTimeScore(desiredTime, match.getStartTime());
            // 3. 종목 점수: 요청한 종목과 매치의 종목 비교
            double sportScore = calculateSportScore(desiredSport, match.getSportType());
            // 4. 평점 점수: 매치 참여 유저들의 종목 rating 평균을 정규화한 값
            double ratingScore = calculateRatingScore(match);

            // 가중치 적용 (예: 위치 40%, 시간 30%, 종목 20%, 평점 10%)
            double totalScore = locationScore * 0.4 + timeScore * 0.3 + sportScore * 0.2 + ratingScore * 0.1;
            match.setRecommendationScore((int) totalScore);
        }

        // 추천 점수가 높은 순으로 정렬 후 상위 10개 매치 반환
        matches.sort(Comparator.comparing(Match::getRecommendationScore).reversed());
        return matches.subList(0, Math.min(matches.size(), 10));
    }

    /**
     * 위치 점수: 요청자의 위치와 매치에 참여한 유저들의 위치의 평균 거리를 이용.
     * - 평균 거리가 5km 이하면 최대 점수 10
     * - 평균 거리가 20km 이상이면 0
     * - 그 사이에서는 선형 보간
     */
    private double calculateLocationScoreForMatch(double userLat, double userLon, Match match) {
        List<MatchParticipant> participants = matchParticipantRepository.findByMatch(match);
        if (participants == null || participants.isEmpty()) {
            // 참여자가 없으면 기본적으로 최대 점수를 부여 (또는 상황에 맞게 조정)
            return 10;
        }
        double totalDistance = 0;
        int count = 0;
        for (MatchParticipant participant : participants) {
            User participantUser = participant.getUser();
            double pLat = participantUser.getLatitude();
            double pLon = participantUser.getLongitude();
            double distance = haversineDistance(userLat, userLon, pLat, pLon);
            totalDistance += distance;
            count++;
        }
        double averageDistance = totalDistance / count;
        if (averageDistance <= 5) return 10;
        if (averageDistance >= 20) return 0;
        return 10 * (20 - averageDistance) / 15.0;
    }

    /**
     * 두 좌표간 거리를 계산 (Haversine 공식)
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * 시간 점수: 원하는 시작 시간과 매치 시작 시간의 차이에 따라 선형 보간하여 계산
     * - 30분 이내면 10점, 2시간 이상이면 0점
     */
    private double calculateTimeScore(LocalDateTime desiredTime, LocalDateTime matchTime) {
        long diffMinutes = Math.abs(Duration.between(desiredTime, matchTime).toMinutes());
        if (diffMinutes <= 30) return 10;
        if (diffMinutes >= 120) return 0;
        return 10.0 * (120 - diffMinutes) / 90.0;
    }

    /**
     * 종목 점수: 요청한 종목과 매치의 종목이 일치하면 10점, 아니면 0
     */
    private double calculateSportScore(String desiredSport, String matchSport) {
        if (desiredSport.equalsIgnoreCase(matchSport)) {
            return 10;
        }
        return 0;
    }

    /**
     * 평점 점수: 매치에 참여한 유저들의 종목별 rating 평균을 이용하여 정규화
     * 예시에서는 rating 범위를 800 ~ 1200으로 가정하여 0 ~ 10 사이 값으로 변환
     */
    private double calculateRatingScore(Match match) {
        List<MatchParticipant> participants = matchParticipantRepository.findByMatch(match);
        if (participants == null || participants.isEmpty()) {
            return 10;
        }
        double sum = 0;
        int count = 0;
        for (MatchParticipant participant : participants) {
            User user = participant.getUser();
            double userSportRating;
            switch (match.getSportType().toLowerCase()) {
                case "badminton":
                    userSportRating = user.getBadmintonRating();
                    break;
                case "pingpong":
                    userSportRating = user.getPingpongRating();
                    break;
                case "futsal":
                    userSportRating = user.getFutsalRating();
                    break;
                default:
                    userSportRating = 1000;
            }
            sum += userSportRating;
            count++;
        }
        double avgRating = sum / count;
        double normalizedScore = (avgRating - 800) * 10.0 / 400;
        if (normalizedScore < 0) normalizedScore = 0;
        if (normalizedScore > 10) normalizedScore = 10;
        return normalizedScore;
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


        // 경기 스코어 DB 저장 (기존 코드 유지)
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

        // 1. 세트 점수 차이 계산 : 각 세트의 점수 차이의 절대값 합산
        int totalScoreDiff = 0;
        for (int i = 0; i < redTeamScores.size(); i++) {
            totalScoreDiff += Math.abs(redTeamScores.get(i) - blueTeamScores.get(i));
        }

        // 2. 팀별 평균 레이팅 계산 : 해당 종목의 레이팅을 사용 (예시에서는 배드민턴)
        List<MatchParticipant> winners = matchParticipantList.stream()
                .filter(mp -> mp.getTeam() == requestDTO.getWinnerTeam())
                .toList();
        List<MatchParticipant> losers = matchParticipantList.stream()
                .filter(mp -> mp.getTeam() != requestDTO.getWinnerTeam())
                .toList();

        double winnerAvgRating = winners.stream()
                .mapToInt(mp -> getSportRating(mp.getUser(), match.getSportType()))
                .average().orElse(1000);
        double loserAvgRating = losers.stream()
                .mapToInt(mp -> getSportRating(mp.getUser(), match.getSportType()))
                .average().orElse(1000);

        // 3. 평점 차이에 따른 multiplier 계산
        double ratingDiff = loserAvgRating - winnerAvgRating; // 승리팀이 언더독이면 양수
        double ratingMultiplier = 1 + (ratingDiff / 400.0);
        // 필요에 따라 multiplier의 범위를 제한 (예: 0.5 ~ 2.0)
        if (ratingMultiplier < 0.5) ratingMultiplier = 0.5;
        if (ratingMultiplier > 2.0) ratingMultiplier = 2.0;

        // 4. 최종 레이팅 변경치 계산 : 기본치 10 + 세트 차이 반영, 여기에 ratingMultiplier 적용
        double adjustmentFactor = 1.0;  // 세트 점수 차이에 적용할 추가 가중치 (테스트를 통해 튜닝)
        int baseDelta = (int) Math.round(10 + adjustmentFactor * totalScoreDiff);
        int ratingDelta = (int) Math.round(baseDelta * ratingMultiplier);

        // 승리 팀과 패배 팀에 적용 (예시에서는 배드민턴 레이팅 업데이트)
        for (MatchParticipant participant : matchParticipantList) {
            User user = userRepository.findById(participant.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없음"));
            if (requestDTO.getWinnerTeam() == participant.getTeam()) {
                // 승리 팀이면 ratingDelta 만큼 상승
                user.setBadmintonRating(user.getBadmintonRating() + ratingDelta);
            } else {
                // 패배 팀이면 ratingDelta 만큼 하락
                user.setBadmintonRating(user.getBadmintonRating() - ratingDelta);
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

    private int getSportRating(User user, String sportType) {
        switch (sportType.toLowerCase()) {
            case "badminton":
                return user.getBadmintonRating();
            case "pingpong":
                return user.getPingpongRating();
            case "futsal":
                return user.getFutsalRating();
            default:
                // 알 수 없는 종목이면 기본값 사용 (예: 1000점)
                return 1000;
        }
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