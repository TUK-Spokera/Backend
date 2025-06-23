package graduation.spokera.api.domain.type;

public enum MatchType {
    ONE_VS_ONE(2),
    TWO_VS_TWO(4),
    FIVE_VS_FIVE(5),
    TEAM_VS_TEAM(10);

    private final int maxParticipants;

    MatchType(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }
}
