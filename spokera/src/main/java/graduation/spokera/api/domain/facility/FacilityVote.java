package graduation.spokera.api.domain.facility;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facilityVoteId;

    @ManyToOne
    @JoinColumn(name = "faci_id")
    private Facility facility;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

}
