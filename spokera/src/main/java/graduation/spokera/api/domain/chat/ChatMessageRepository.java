package graduation.spokera.api.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByMatch_MatchIdOrderBySentAtAsc(Long matchMatchId);
}
