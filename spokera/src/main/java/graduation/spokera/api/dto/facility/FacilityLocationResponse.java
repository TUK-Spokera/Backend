package graduation.spokera.api.dto.facility;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FacilityLocationResponse {
    private double latitude;
    private double longitude;
    private int altitude = 10;
}
