package graduation.spokera.api.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteMessageDTO {
    private Long matchId;
    private String facilityName;
}
