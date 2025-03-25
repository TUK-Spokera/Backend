package graduation.spokera.api.repository;

import graduation.spokera.api.dto.match.MatchSubmissionDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MatchSubmissionMemoryStore {

    // matchId를 key로, 해당 매치에 제출된 MatchSubmissionDTO 리스트를 저장
    private static final Map<Long, List<MatchSubmissionDTO>> submissions = new ConcurrentHashMap<>();

    // 제출 정보를 추가 (동시성 고려하여 ConcurrentHashMap 사용)
    public static void addSubmission(Long matchId, MatchSubmissionDTO submission) {
        submissions.computeIfAbsent(matchId, k -> new ArrayList<>()).add(submission);
    }

    // 해당 matchId에 제출된 MatchSubmissionDTO 리스트 반환
    public static List<MatchSubmissionDTO> getSubmissions(Long matchId) {
        return submissions.getOrDefault(matchId, new ArrayList<>());
    }

    // 매치의 제출 내역 초기화 (예: 매치 완료 후)
    public static void clearSubmissions(Long matchId) {
        submissions.remove(matchId);
    }
}