package graduation.spokera.api.repository;

import graduation.spokera.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 사용자 위치 업데이트 (JPQL 사용)
    @Modifying
    @Query("UPDATE User u SET u.latitude = :latitude, u.longitude = :longitude WHERE u.userId = :userId")
    void updateUserLocation(Long userId, double latitude, double longitude);

    // 사용자 실력 점수 업데이트
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.rating = :rating WHERE u.userId = :userId")
    void updateUserRating(Long userId, int rating);

    Optional<User> findByUsername(String username);
}