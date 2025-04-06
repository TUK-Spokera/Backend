package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.match.SetScore;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SetScoreResponseDTO {
    private Integer setNumber;
    private Integer redTeamScore;
    private Integer blueTeamScore;

    public static SetScoreResponseDTO toDTO(SetScore setScore) {
        return new SetScoreResponseDTO(
                setScore.getSetNumber(),
                setScore.getRedTeamScore(),
                setScore.getBlueTeamScore()
        );
    }
}
