package graduation.spokera.api.domain.match;

import graduation.spokera.api.domain.type.*;
import graduation.spokera.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "matches")
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    private String sportType;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    // ✅ 경기장 투표로 결정된 최종 경기장 이름
    @Column(name = "selected_facility_name")
    private String selectedFacilityName;

    @Transient
    private Integer recommendationScore;

    @Transient
    private Double averageDistance;

    @Transient
    private String skillGapText;  // 실력 차이를 텍스트로 표현 (예: "비슷한 실력", "조금 더 높은 실력" 등)

    @Enumerated(EnumType.STRING)
    private TeamType winnerTeam;
}