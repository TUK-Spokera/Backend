package graduation.spokera.api.model;

import graduation.spokera.api.model.enums.MatchStatus;
import graduation.spokera.api.model.enums.MatchType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "matches")
@ToString
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String sportType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    @OneToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    public Match() {
        this.status = MatchStatus.WAITING;  // 기본 상태는 WAITING
    }
}