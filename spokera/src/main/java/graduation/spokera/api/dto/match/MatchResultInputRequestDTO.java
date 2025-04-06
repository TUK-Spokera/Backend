package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.type.MatchResult;
import graduation.spokera.api.domain.type.TeamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MatchResultInputRequestDTO {
    private Long matchId;
    private List<Integer> redTeamScores;
    private List<Integer> blueTeamScores;
    private TeamType winnerTeam;
}
