package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.type.MatchResult;
import graduation.spokera.api.dto.user.UserSubmissionInfoDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchSubmissionDTO {
    private UserSubmissionInfoDTO user;
    private MatchResult matchResult;
}