package graduation.spokera.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLocationDTO {
    private String username;
    private Double latitude;
    private Double longitude;
}
