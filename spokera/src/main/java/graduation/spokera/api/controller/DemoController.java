package graduation.spokera.api.controller;

import graduation.spokera.api.domain.match.Match;
import graduation.spokera.api.domain.match.MatchParticipant;
import graduation.spokera.api.domain.type.MatchStatus;
import graduation.spokera.api.repository.MatchParticipantRepository;
import graduation.spokera.api.repository.MatchRepository;
import graduation.spokera.api.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {

    private final MatchRepository matchRepository;
    private final MatchService matchService;
    private final MatchParticipantRepository matchParticipantRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Match> waitMatches = matchRepository.findByStatus(MatchStatus.WAITING);

        List<MatchParticipant> waitMatchparticipants = new ArrayList<>();

        // matchId → 참가자 리스트 매핑
        Map<Long, List<MatchParticipant>> participantsMap = new HashMap<>();

        for (Match m : waitMatches) {
            List<MatchParticipant> list = matchParticipantRepository.findByMatch(m);
            participantsMap.put(m.getMatchId(), list);
        }

        model.addAttribute("waitMatches", waitMatches);
        model.addAttribute("participantsMap", participantsMap);

        return "dashboard";    }
}
