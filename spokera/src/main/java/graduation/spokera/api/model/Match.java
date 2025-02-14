package graduation.spokera.api.model;

import graduation.spokera.api.model.enums.MatchStatus;
import graduation.spokera.api.model.enums.MatchType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

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

}