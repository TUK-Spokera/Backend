package graduation.spokera.api.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatEnterDTO {
    private Long matchId;
    private String nickname;
    private String message;
}
