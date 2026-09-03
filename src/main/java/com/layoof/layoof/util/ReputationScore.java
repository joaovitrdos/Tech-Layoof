package com.layoof.layoof.util;

public final class ReputationScore {

    public static final int INITIAL = 50;

    private static final int STEP = 5;
    private static final int MIN = 0;
    private static final int MAX = 100;

    private ReputationScore() {
    }

    public static int apply(int score, long reactBalance) {
        if (reactBalance == 0) {
            return Math.clamp(score, MIN, MAX);
        }
        return Math.clamp(reactBalance > 0 ? score + STEP : score - STEP, MIN, MAX);
    }
}
