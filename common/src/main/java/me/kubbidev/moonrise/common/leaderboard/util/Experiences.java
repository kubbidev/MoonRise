package me.kubbidev.moonrise.common.leaderboard.util;

public final class Experiences {

    private Experiences() {
    }

    /**
     * Calculates the amount of experience required to reach a specific level.
     *
     * @param currentLevel the level for which the experience requirement is being calculated
     * @return the total experience required to reach the specified level
     */
    public static long calculateExperienceForLevel(int currentLevel) {
        return 5 * ((long) currentLevel * currentLevel) + (50L * currentLevel) + 100;
    }

    /**
     * Determines the level a player has reached based on the total experience points.
     *
     * @param totalExperience the total experience points accumulated
     * @return the current level the player has achieved
     */
    public static int determineLevelFromExperience(long totalExperience) {
        int currentLevel = 0;

        while (totalExperience >= calculateExperienceForLevel(currentLevel)) {
            totalExperience -= calculateExperienceForLevel(currentLevel);
            currentLevel++;
        }

        return currentLevel;
    }
}
