package graduation.spokera.api.repository;

import graduation.spokera.api.model.Match;
import graduation.spokera.api.model.MatchParticipant;
import graduation.spokera.api.model.User;
import graduation.spokera.api.model.enums.MatchStatus;
import graduation.spokera.api.model.enums.MatchType;
import org.hibernate.sql.results.graph.FetchList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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