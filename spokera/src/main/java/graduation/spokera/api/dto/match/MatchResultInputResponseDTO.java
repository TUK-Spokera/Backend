package graduation.spokera.api.dto.match;

import lombok.*;

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