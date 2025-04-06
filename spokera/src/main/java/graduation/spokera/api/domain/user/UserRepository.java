package graduation.spokera.api.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(String kakaoId);
    Optional<User> findByNickname(String nickname);
    List<User> findAllByOrderByBadmintonRatingDesc();
    List<User> findAllByOrderByFutsalRating();
    List<User> findAllByOrderByPingpongRating();
}