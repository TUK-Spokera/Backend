package graduation.spokera.api.domain.match;

import graduation.spokera.api.domain.type.MatchResult;
import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.domain.type.MatchType;
import graduation.spokera.api.domain.type.TeamType;
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

    @Transient
    private Integer recommendationScore;

    @Enumerated(EnumType.STRING)
    private TeamType winnerTeam;

}