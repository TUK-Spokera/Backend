package graduation.spokera.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class UserBasicInfoDTO {
    private Long userId;
    private String nickname;
}
