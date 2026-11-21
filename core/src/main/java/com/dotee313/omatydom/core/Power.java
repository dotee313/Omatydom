package com.dotee313.omatydom.core;

/** Player-triggered world powers. Original names and effects, not copied art. */
public enum Power {
    RAIN("Life Rain", "Refills nearby forests, orchards and mountains.", 30, 10),
    BLOOM("Green Tide", "Regrows a wide area and adds food to the stores.", 50, 15),
    HEAL("Mending Light", "Restores the health of everyone near the target.", 40, 20),
    BLESS("Warm Favor", "Raises happiness and loyalty near the target.", 60, 35),
    LIGHTNING("Storm Spear", "Strikes people and animals in a small area.", 45, 25),
    PLAGUE("Gray Sickness", "A dangerous fever spreads through a small area.", 90, 30),
    EARTHQUAKE("Earth Shake", "Cracks the ground and damages nearby structures.", 80, 30),
    METEOR("Sky Fire", "A burning rock devastates the target area.", 180, 80),
    PEACE("Quiet Minds", "Calms the whole island and lifts stability.", 120, 40),
    PROSPERITY("Golden Rain", "Fills the treasury and enriches every household.", 100, 45);

    private final String displayName;
    private final String description;
    private final int cooldownDays;
    private final int goldCost;

    Power(String displayName, String description, int cooldownDays, int goldCost) {
        this.displayName = displayName;
        this.description = description;
        this.cooldownDays = cooldownDays;
        this.goldCost = goldCost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getCooldownDays() {
        return cooldownDays;
    }

    public int getGoldCost() {
        return goldCost;
    }
}
