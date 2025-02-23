package graduation.spokera.api.domain.match;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.domain.type.MatchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {
    List<MatchParticipant> findByUser(User user);

    // 특정 사용자가 특정 타입의 매칭에 이미 참가했는지 확인
    boolean existsByUserAndMatch_SportTypeAndMatch_MatchTypeAndMatch_Status(
            User user, String sportType, MatchType matchType, MatchStatus status
    );

    List<MatchParticipant> findByMatch(Match match);
}