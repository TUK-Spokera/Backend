package graduation.spokera.api.dto;

import graduation.spokera.api.model.Facility;
import graduation.spokera.api.model.enums.MatchStatus;
import graduation.spokera.api.model.enums.MatchType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MatchResponseDTO {
    private Long matchId;
    private MatchStatus status;
}