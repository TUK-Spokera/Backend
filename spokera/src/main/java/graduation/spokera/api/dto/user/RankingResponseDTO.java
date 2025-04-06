package graduation.spokera.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingResponseDTO {
    private List<UserRatingInfoDTO> badmintonRanking;
    private List<UserRatingInfoDTO> pingpongRanking;
    private List<UserRatingInfoDTO> futsalRanking;

}
