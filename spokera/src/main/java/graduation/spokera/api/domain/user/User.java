package graduation.spokera.api.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키

    @Column(nullable = false, unique = true)
    private String kakaoId; // 카카오 사용자 ID (unique)

    private String nickname;
    private String email;

    private Double latitude;  // 현재 위치 위도
    private Double longitude; // 현재 위치 경도

//    private Integer rating; // 레이팅 3개로 분리

    private Integer badmintonRating;
    private Integer pingpongRating;
    private Integer futsalRating;

    private LocalDateTime createdAt; // 가입일

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.badmintonRating = 1000;
        this.pingpongRating = 1000;
        this.futsalRating = 1000;
    }
}