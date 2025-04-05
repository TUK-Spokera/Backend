package graduation.spokera.api.repository;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.type.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStatus(MatchStatus matchStatus);
}