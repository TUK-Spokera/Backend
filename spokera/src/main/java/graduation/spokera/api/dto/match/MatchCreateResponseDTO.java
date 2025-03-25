package graduation.spokera.api.dto.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchCreateResponseDTO {
    private Long matchId;
    private boolean success;
    private String message;
}
