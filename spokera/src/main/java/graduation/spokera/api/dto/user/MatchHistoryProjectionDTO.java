package graduation.spokera.api.dto.user;

import graduation.spokera.api.domain.type.MatchResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class MatchHistoryProjectionDTO {
    private Long matchId;
    private String sportType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String result; // WIN or LOSE
}
