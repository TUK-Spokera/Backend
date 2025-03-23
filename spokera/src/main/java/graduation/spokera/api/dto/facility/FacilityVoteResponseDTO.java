package graduation.spokera.api.dto.facility;

import graduation.spokera.api.domain.facility.FacilityVote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FacilityVoteResponseDTO {
    private boolean success;
    private String message;
    private FacilityVoteRequestDTO facilityVoteRequestDTO;
}
