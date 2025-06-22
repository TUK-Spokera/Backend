package graduation.spokera.api.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatTeamInfoResponse {
    private String type;
    private Long matchId;
    private Map<String, List<String>> teams;
}
