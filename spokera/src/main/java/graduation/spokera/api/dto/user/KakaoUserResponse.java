package graduation.spokera.api.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserResponse {
    private Long id;
    private String nickname;
    private String email;
}
