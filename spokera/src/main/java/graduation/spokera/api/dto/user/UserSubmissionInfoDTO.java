package graduation.spokera.api.dto.user;

import graduation.spokera.api.domain.type.TeamType;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@Builder
@ToString
public class UserSubmissionInfoDTO {
    private Long userId;
    private String nickname;
    private Integer rating;
    private TeamType team;
}
