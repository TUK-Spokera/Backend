package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.type.MatchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MatchResultInputRequestDTO {
    private Long matchId;
    private Long userId;
    private MatchResult matchResult;
}
