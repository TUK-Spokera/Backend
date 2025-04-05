package graduation.spokera.api.domain.match;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 매치에 속한 세트인지
    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private Integer setNumber; // 1세트, 2세트...

    private Integer redTeamScore;
    private Integer blueTeamScore;
}