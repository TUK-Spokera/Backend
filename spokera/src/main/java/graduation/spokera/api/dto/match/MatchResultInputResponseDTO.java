package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.match.SetScore;
import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.domain.user.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MatchResultInputResponseDTO {
    private Long matchId;
    private boolean success;
    private String message;
}