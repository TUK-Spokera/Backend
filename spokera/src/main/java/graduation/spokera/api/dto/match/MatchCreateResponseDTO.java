package graduation.spokera.api.dto.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchCreateResponseDTO {
    private Long createdMatchId;
    private boolean success;
    private String message;
}
