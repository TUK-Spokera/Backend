package graduation.spokera.api.dto.facility;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 경기장 정보 줄때 필요한 정보 DTO
 */
@Setter
@Getter
@Builder
public class FacilityRecommendResponseDTO {
    private Integer id;
    private Double faciLat;
    private Double faciLot;
    private String ftypeNm;
    private String faciNm;
    private String fcobNm;
    private String faciAddr;
    private String url;
}
