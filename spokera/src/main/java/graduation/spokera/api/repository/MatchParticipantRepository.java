package graduation.spokera.api.repository;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {
    List<MatchParticipant> findByUser(User user);
    List<MatchParticipant> findByMatch(Match match);
    int countByMatch(Match match);
    boolean existsByMatchAndUserId(Match match, Long userId);
    Optional<MatchParticipant> findByMatchAndUser(Match match, User user);

    List<MatchParticipant> findByMatchIn(Collection<Match> matches);
}