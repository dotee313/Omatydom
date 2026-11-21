package com.dotee313.omatydom.core;

/** A direct order issued from the throne. Effects are applied to the whole island. */
public enum Decree {
    FESTIVAL(
            "Public Festival",
            "Music, food and lanterns fill the shoreline.",
            -10, 12, 3, 8, 1),
    TAX_CUT(
            "Tax Cut",
            "The treasury empties, but the people breathe easier.",
            -25, 10, -2, 12, 4),
    TAX_RAISE(
            "Tax Raise",
            "Coin flows in, goodwill flows out.",
            35, -10, 3, -12, -3),
    BUILD_TEMPLE(
            "Build a Temple",
            "Stone rises on the hill and the faithful gather.",
            -30, 4, 8, 5, 0),
    AMNESTY(
            "General Amnesty",
            "Prison doors open and old grudges soften.",
            -5, 8, 5, 10, 0),
    CONSCRIPTION(
            "Conscription",
            "Every household sends a soldier to the shore guard.",
            10, -12, 10, -6, 0),
    FREE_TRADE(
            "Free Trade",
            "Foreign ships are finally welcome at the harbour.",
            20, 5, 2, 3, 8),
    CRACKDOWN(
            "Crackdown",
            "The guard patrols every street. Order returns, silence spreads.",
            0, -15, 15, -10, -2);

    private final String displayName;
    private final String description;
    private final double goldEffect;
    private final double happinessEffect;
    private final double stabilityEffect;
    private final double loyaltyEffect;
    private final double wealthEffect;

    Decree(String displayName, String description,
           double goldEffect, double happinessEffect, double stabilityEffect,
           double loyaltyEffect, double wealthEffect) {
        this.displayName = displayName;
        this.description = description;
        this.goldEffect = goldEffect;
        this.happinessEffect = happinessEffect;
        this.stabilityEffect = stabilityEffect;
        this.loyaltyEffect = loyaltyEffect;
        this.wealthEffect = wealthEffect;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public double getGoldEffect() {
        return goldEffect;
    }

    public double getHappinessEffect() {
        return happinessEffect;
    }

    public double getStabilityEffect() {
        return stabilityEffect;
    }

    public double getLoyaltyEffect() {
        return loyaltyEffect;
    }

    public double getWealthEffect() {
        return wealthEffect;
    }
}
