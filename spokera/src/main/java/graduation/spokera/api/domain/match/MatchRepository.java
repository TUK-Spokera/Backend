package graduation.spokera.api.domain.match;

import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.domain.type.MatchType;
import graduation.spokera.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {


    @Query("SELECT DISTINCT u FROM User u JOIN MatchParticipant mp ON u.userId = mp.user.userId " +
            "WHERE mp.match.sportType = :sportType AND mp.match.matchType = :matchType")
    List<User> findMatchedUsers(@Param("sportType") String sportType, @Param("matchType") MatchType matchType);

    @Query("SELECT m FROM Match m WHERE m.sportType = :sportType " +
            "AND m.status = :matchStatus " +
            "AND m.matchType = :matchType " +
            "AND m.startTime < :endTime " +
            "AND m.endTime > :startTime")
    Optional<Match> findAvailableMatch(String sportType, LocalDateTime startTime, LocalDateTime endTime, MatchType matchType, MatchStatus matchStatus);


    List<Match> findByStatus(MatchStatus matchStatus);
}