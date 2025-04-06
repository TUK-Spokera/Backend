package graduation.spokera.api.controller;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.user.UserRepository;
import graduation.spokera.api.dto.user.RankingResponseDTO;
import graduation.spokera.api.dto.user.UserRatingInfoDTO;
import graduation.spokera.api.service.MatchService;
import graduation.spokera.api.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 유저 종목 별 랭킹 조회
     */
    @GetMapping
    public RankingResponseDTO getRankingList() {

        return rankingService.getRankingList();

    }


}
