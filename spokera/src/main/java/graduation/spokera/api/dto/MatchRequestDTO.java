package graduation.spokera.api.dto;

import graduation.spokera.api.model.enums.MatchType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchRequestDTO {
    private String username;
    private String sportType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MatchType matchType;
//    private double latitude;
//    private double longitude;
}