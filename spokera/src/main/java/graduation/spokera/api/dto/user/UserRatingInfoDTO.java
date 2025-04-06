package graduation.spokera.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRatingInfoDTO {
    private Integer rank;
    private Long userId;
    private String nickname;
    private Integer rating;
}
