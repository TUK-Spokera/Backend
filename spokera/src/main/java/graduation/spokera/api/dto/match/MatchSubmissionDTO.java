package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.type.MatchResult;
import graduation.spokera.api.domain.user.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchSubmissionDTO {
    private User user;
    private MatchResult matchResult;
}