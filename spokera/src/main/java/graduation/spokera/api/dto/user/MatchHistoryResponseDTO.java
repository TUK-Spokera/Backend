package graduation.spokera.api.dto.user;

import graduation.spokera.api.domain.match.SetScore;
import graduation.spokera.api.domain.type.MatchResult;
import graduation.spokera.api.domain.type.MatchType;
import graduation.spokera.api.domain.type.TeamType;
import graduation.spokera.api.dto.match.SetScoreResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchHistoryResponseDTO {
    private Long matchId;
    private String sportType;
    private List<SetScoreResponseDTO> setScoreResponseDTOList;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TeamType teamType;
    private MatchResult result; // WIN or LOSE


}
