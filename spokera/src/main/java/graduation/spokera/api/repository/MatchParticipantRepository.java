package graduation.spokera.api.repository;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.domain.type.MatchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {
    List<MatchParticipant> findByUser(User user);
    List<MatchParticipant> findByMatch(Match match);
    int countByMatch(Match match);
    boolean existsByMatchAndUserId(Match match, Long userId);

}