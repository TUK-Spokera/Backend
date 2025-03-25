package graduation.spokera.api.dto.match;

import graduation.spokera.api.domain.user.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MatchResultInputResponseDTO {
    private Long matchId;
    private List<MatchSubmissionDTO> submissions; // 유저와 승패 정보를 함께 저장
    private boolean isMatchCompleted;
}