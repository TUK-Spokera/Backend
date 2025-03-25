package graduation.spokera.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationDTO {
    private String userId;
    private String username;
    private Double latitude;
    private Double longitude;
    private Long timestamp;
}
