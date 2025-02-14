package graduation.spokera.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatMessageId;

    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private LocalDateTime sentAt;
}
