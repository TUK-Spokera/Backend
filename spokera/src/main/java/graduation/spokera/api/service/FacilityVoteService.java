package graduation.spokera.api.service;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.repository.MatchRepository;
import graduation.spokera.api.repository.MatchParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



@Service
@RequiredArgsConstructor
public class FacilityVoteService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    private final Map<Long, Map<String, Integer>> voteCache = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, String>> userVoteHistory = new ConcurrentHashMap<>();

    public void voteFacility(Long matchId, Long userId, String newFacilityName) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        boolean isParticipant = matchParticipantRepository.existsByMatchAndUserId(match, userId);

        if (!isParticipant) {
            System.out.println("🚫 매치 참가자가 아님 → 투표 거부됨");
            return;
        }

        // ✅ 이미 선택된 경기장이 있다면 투표 막기
        if (match.getSelectedFacilityName() != null) {
            System.out.println("⚠️ 이미 경기장이 선택됨 → 투표 무시됨");
            return;
        }

        // ✅ 캐시 초기화
        voteCache.putIfAbsent(matchId, new HashMap<>());
        userVoteHistory.putIfAbsent(matchId, new HashMap<>());

        Map<String, Integer> matchVotes = voteCache.get(matchId); // 시설명 → 득표수
        Map<Long, String> userVotes = userVoteHistory.get(matchId); // userId → 시설명

        String previousVote = userVotes.get(userId);

        // ✅ 이전 투표가 다른 시설이었다면 카운트 제거
        if (previousVote != null && !previousVote.equals(newFacilityName)) {
            int prevCount = matchVotes.getOrDefault(previousVote, 1) - 1;
            if (prevCount <= 0) {
                matchVotes.remove(previousVote);
            } else {
                matchVotes.put(previousVote, prevCount);
            }
        }


        // ✅ 새로운 투표 반영 (기존과 같으면 그대로 유지)
        if (!newFacilityName.equals(previousVote)) {
            matchVotes.put(newFacilityName, matchVotes.getOrDefault(newFacilityName, 0) + 1);
            userVotes.put(userId, newFacilityName);
            System.out.println("🔁 사용자 재투표 처리: userId=" + userId + ", newVote=" + newFacilityName);
        } else {
            System.out.println("ℹ️ 동일한 시설로 재투표 → 무시됨");
        }

        // ✅ 과반수 이상이면 경기장 확정
        int totalParticipants = matchParticipantRepository.countByMatch(match);
        int voteCount = matchVotes.getOrDefault(newFacilityName, 0);
        int majority = totalParticipants / 2 + 1;

        if (voteCount >= majority) {
            match.setSelectedFacilityName(newFacilityName);
            matchRepository.save(match);
            System.out.println("✅ 최종 선택 경기장 저장 (과반수 득표): " + newFacilityName);
        }
    }



    public String getSelectedFacility(Long matchId) {
        return matchRepository.findById(matchId)
                .map(Match::getSelectedFacilityName)
                .orElse(null);
    }

    public Map<String, Integer> getVoteResult(Long matchId) {
        return new HashMap<>(voteCache.getOrDefault(matchId, new HashMap<>()));
    }
}

