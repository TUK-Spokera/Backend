package graduation.spokera.api.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageDTO {
    private Long matchId;  // 매칭방 ID
    private String senderName; // ✅ User 객체 대신 senderId만 전달
    private String content;
}