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

    private int rating; // 매칭 시 활용할 실력 점수

    private LocalDateTime createdAt; // 가입일

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.rating = 0;
    }
}