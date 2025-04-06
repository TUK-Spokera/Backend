package graduation.spokera.api.service;

import graduation.spokera.api.domain.user.User;
import graduation.spokera.api.domain.user.UserRepository;
import graduation.spokera.api.dto.user.RankingResponseDTO;
import graduation.spokera.api.dto.user.UserRatingInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankingService {

    private final UserRepository userRepository;

    /**
     * 종목별로 랭킹 정보
     */
    public RankingResponseDTO getRankingList() {
        List<User> userList = userRepository.findAll();

        List<UserRatingInfoDTO> badmintonRankingList = createRankingList(userList, "badminton");
        List<UserRatingInfoDTO> pingpongRankingList = createRankingList(userList, "pingpong");
        List<UserRatingInfoDTO> futsalRankingList = createRankingList(userList, "futsal");

        return RankingResponseDTO.builder()
                .badmintonRanking(badmintonRankingList)
                .pingpongRanking(pingpongRankingList)
                .futsalRanking(futsalRankingList)
                .build();
    }

    private List<UserRatingInfoDTO> createRankingList(List<User> users, String sport) {
        Comparator<User> comparator;
        Function<User, Integer> ratingGetter;

        switch (sport) {
            case "badminton" -> {
                comparator = Comparator.comparing(User::getBadmintonRating, Comparator.nullsLast(Integer::compareTo)).reversed();
                ratingGetter = User::getBadmintonRating;
            }
            case "pingpong" -> {
                comparator = Comparator.comparing(User::getPingpongRating, Comparator.nullsLast(Integer::compareTo)).reversed();
                ratingGetter = User::getPingpongRating;
            }
            case "futsal" -> {
                comparator = Comparator.comparing(User::getFutsalRating, Comparator.nullsLast(Integer::compareTo)).reversed();
                ratingGetter = User::getFutsalRating;
            }
            default -> throw new IllegalArgumentException("Unsupported sport: " + sport);
        }

        List<User> sortedUsers = users.stream()
                .sorted(comparator)
                .toList();

        List<UserRatingInfoDTO> rankingList = new ArrayList<>();
        int rank = 1;
        int count = 1;
        Integer previousRating = null;

        for (User user : sortedUsers) {
            Integer currentRating = ratingGetter.apply(user);
            if (currentRating == null) continue;

            if (previousRating != null && !currentRating.equals(previousRating)) {
                rank = count;
            }

            rankingList.add(UserRatingInfoDTO.builder()
                    .rank(rank)
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .rating(currentRating)
                    .build());

            previousRating = currentRating;
            count++;
        }

        return rankingList;
    }

}
