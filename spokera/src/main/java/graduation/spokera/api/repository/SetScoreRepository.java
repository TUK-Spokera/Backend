package graduation.spokera.api.repository;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.SetScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetScoreRepository extends JpaRepository<SetScore, Long> {

    List<SetScore> findByMatch(Match match);
}
