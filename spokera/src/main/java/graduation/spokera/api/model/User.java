package graduation.spokera.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username; // 사용자 이름

    private Double latitude; // 현재 위치 위도
    private Double longitude; // 현재 위치 경도

    private LocalDateTime createdAt; // 계정 생성 시간

    private int rating; // 실력 점수 (매칭 시 사용)

    // 엔티티가 저장되기 전에 자동으로 실행
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.rating = 0;
    }
}