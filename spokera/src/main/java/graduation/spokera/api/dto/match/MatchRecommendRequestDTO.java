package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.type.MatchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchRecommendRequestDTO {
    private Long userId;
    private String sportType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MatchType matchType;
    private Double latitude;
    private Double longitude;
}