package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.type.MatchResult;
import graduation.spokera.api.dto.user.UserBasicInfoDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchSubmissionDTO {
    private UserBasicInfoDTO user;
    private MatchResult matchResult;
}