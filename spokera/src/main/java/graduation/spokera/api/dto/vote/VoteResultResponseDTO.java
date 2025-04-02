package graduation.spokera.api.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteResultResponseDTO {
    private String type = "VOTE_RESULT";
    private Long matchId;
    private Map<String, Integer> voteCounts;
    private String selectedFacility;
}
