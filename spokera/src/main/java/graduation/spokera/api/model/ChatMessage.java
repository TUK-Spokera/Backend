package graduation.spokera.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatMessageId;
    private String sender;
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
