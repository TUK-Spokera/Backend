package graduation.spokera.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCoordinatesDto {
    private double user1Lat;
    private double user1Lot;
    private double user2Lat;
    private double user2Lot;
    private int maxResults;
}