package com.dotee313.omatydom.core.model;

/** A spreading thought. Ideas move between neighbours and nudge mood and loyalty. */
public enum Idea {
    SURVIVAL("Survival", 0.0, 0.0),
    PROGRESS("Progress", 2.5, -1.0),
    MYSTICISM("Mysticism", 2.0, 1.0),
    REBELLION("Rebellion", -7.0, -9.0),
    PEACE("Peace", 3.5, 3.0),
    TRADE("Trade", 2.5, 2.0),
    CURIOSITY("Curiosity", 2.0, 0.0);

    private final String displayName;
    private final double happinessEffect;
    private final double loyaltyEffect;

    Idea(String displayName, double happinessEffect, double loyaltyEffect) {
        this.displayName = displayName;
        this.happinessEffect = happinessEffect;
        this.loyaltyEffect = loyaltyEffect;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getHappinessEffect() {
        return happinessEffect;
    }

    public double getLoyaltyEffect() {
        return loyaltyEffect;
    }
}
