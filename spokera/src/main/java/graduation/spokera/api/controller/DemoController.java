package graduation.spokera.api.controller;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.domain.type.MatchType;
import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.user.UserRepository;
import graduation.spokera.api.dto.match.MatchRecommendRequestDTO;
import graduation.spokera.api.repository.MatchParticipantRepository;
import graduation.spokera.api.repository.MatchRepository;
import graduation.spokera.api.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {

    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final MatchParticipantRepository matchParticipantRepository;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Match> waitMatches = matchRepository.findByStatus(MatchStatus.WAITING);

        // matchId → 참가자 리스트 매핑
        Map<Long, List<MatchParticipant>> participantsMap = new HashMap<>();

        // matchId -> 평균 레이팅 점수 매핑
        Map<Long, Double> avgRatingMap = new HashMap<>();

        for (Match m : waitMatches) {

            Long matchId = m.getMatchId();

            List<MatchParticipant> list = matchParticipantRepository.findByMatch(m);

            // 평균 레이팅
            int ratingSum = 0;
            for (MatchParticipant mp : list) {
                ratingSum += mp.getUser().getBadmintonRating();
            }
            double avgRating = (double) ratingSum / list.size();

            avgRatingMap.put(matchId, avgRating);

            //참가자 리스트
            participantsMap.put(matchId, list);
        }

        model.addAttribute("waitMatches", waitMatches);
        model.addAttribute("participantsMap", participantsMap);
        model.addAttribute("avgRatingMap", avgRatingMap);

        return "dashboard";
    }
}
