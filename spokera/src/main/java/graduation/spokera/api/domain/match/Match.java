package graduation.spokera.api.domain.match;

import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.domain.type.MatchType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "matches")
@ToString
@NoArgsConstructor
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

}