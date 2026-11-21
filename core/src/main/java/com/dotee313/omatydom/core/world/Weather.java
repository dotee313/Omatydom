package com.dotee313.omatydom.core.world;

/** Island weather. Changes work output, resource regrowth, mood and structures. */
public enum Weather {
    CLEAR("Clear skies", 1.00, 1.00, 0.0, 0.0),
    RAIN("Life rain", 1.05, 1.70, 0.5, 0.0),
    STORM("Storm", 0.78, 1.20, -2.5, 0.025),
    DROUGHT("Drought", 0.88, 0.30, -1.5, 0.0),
    SNOW("Snowfall", 0.72, 0.40, -1.0, 0.0),
    ASH("Volcanic ash", 0.60, 0.20, -3.0, 0.05);

    private final String displayName;
    private final double workMultiplier;
    private final double regrowthMultiplier;
    private final double moodDelta;
    private final double structureDamageChance;

    Weather(String displayName, double workMultiplier, double regrowthMultiplier,
            double moodDelta, double structureDamageChance) {
        this.displayName = displayName;
        this.workMultiplier = workMultiplier;
        this.regrowthMultiplier = regrowthMultiplier;
        this.moodDelta = moodDelta;
        this.structureDamageChance = structureDamageChance;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getWorkMultiplier() {
        return workMultiplier;
    }

    public double getRegrowthMultiplier() {
        return regrowthMultiplier;
    }

    public double getMoodDelta() {
        return moodDelta;
    }

    public double getStructureDamageChance() {
        return structureDamageChance;
    }
}
