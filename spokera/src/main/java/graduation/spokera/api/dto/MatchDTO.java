package graduation.spokera.api.dto;

import graduation.spokera.api.model.Facility;
import graduation.spokera.api.model.enums.MatchStatus;
import graduation.spokera.api.model.enums.MatchType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MatchDTO {
    private Long matchId;
    private String sportType;
    private MatchStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
