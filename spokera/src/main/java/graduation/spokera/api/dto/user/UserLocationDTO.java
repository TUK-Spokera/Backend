package graduation.spokera.api.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLocationDTO {
    private String username;
    private Double latitude;
    private Double longitude;
}
