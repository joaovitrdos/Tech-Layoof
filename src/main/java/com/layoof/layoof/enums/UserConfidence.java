package com.layoof.layoof.enums;

public enum UserConfidence {

    LOW(25, 1),
    MEDIUM(50, 2),
    GOOD(75, 3),
    HIGH(100, 4);

    private final int maxScore;
    private final int badges;

    UserConfidence(int maxScore, int badges) {
        this.maxScore = maxScore;
        this.badges = badges;
    }

    public static UserConfidence of(int score) {
        for (UserConfidence confidence : values()) {
            if (score <= confidence.maxScore) {
                return confidence;
            }
        }
        return HIGH;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public int getBadges() {
        return badges;
    }
}
