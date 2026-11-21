package com.dotee313.omatydom.core;

/** Long eras that slowly change the whole island's mood and fortune. */
public enum Age {
    DAWN("Dawn", 1.00, 1.5, 1.10),
    SUN("High Sun", 1.12, 3.0, 1.15),
    HARVEST("Harvest", 1.18, 4.0, 1.20),
    STORM("Storm", 0.92, -2.5, 0.90),
    MOON("Quiet Moon", 1.00, 1.0, 1.00),
    ASH("Ash", 0.82, -4.0, 0.75),
    ICE("Long Ice", 0.78, -3.0, 0.70),
    RENEWAL("Renewal", 1.10, 3.5, 1.25);

    private final String displayName;
    private final double workMultiplier;
    private final double moodDelta;
    private final double birthMultiplier;

    Age(String displayName, double workMultiplier, double moodDelta, double birthMultiplier) {
        this.displayName = displayName;
        this.workMultiplier = workMultiplier;
        this.moodDelta = moodDelta;
        this.birthMultiplier = birthMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getWorkMultiplier() {
        return workMultiplier;
    }

    public double getMoodDelta() {
        return moodDelta;
    }

    public double getBirthMultiplier() {
        return birthMultiplier;
    }

    public static Age of(int day) {
        int ageIndex = (Math.max(1, day) - 1) / GameConfig.DAYS_PER_AGE;
        return values()[ageIndex % values().length];
    }
}
