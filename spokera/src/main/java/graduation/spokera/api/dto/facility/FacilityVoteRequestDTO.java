package graduation.spokera.api.dto.facility;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class FacilityVoteRequestDTO {
    private Long matchId;
    private Long userId;
    private Integer facilityId;

}
