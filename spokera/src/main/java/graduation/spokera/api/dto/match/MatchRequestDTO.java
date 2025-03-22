package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.type.MatchType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchRequestDTO {
    private String nickname;
    private String sportType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MatchType matchType;
//    private double latitude;
//    private double longitude;
}