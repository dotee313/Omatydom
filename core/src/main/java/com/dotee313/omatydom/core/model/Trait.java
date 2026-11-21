package com.dotee313.omatydom.core.model;

/** Innate personalities that nudge how an islander works, feels and lives. */
public enum Trait {
    BRAVE("Brave", 0.12, 0.0, 0.0, 0.65, 0.0),
    LAZY("Lazy", -0.22, -0.05, 0.0, 0.0, 0.04),
    GENEROUS("Generous", 0.04, 0.18, 0.10, 0.0, 0.0),
    ZEALOUS("Zealous", 0.0, -0.06, 0.22, 0.0, 0.0),
    CURIOUS("Curious", 0.03, 0.06, 0.0, 0.0, 0.10),
    RESILIENT("Resilient", 0.0, 0.04, 0.0, 0.0, 0.35),
    GREEDY("Greedy", 0.16, -0.14, -0.10, 0.0, 0.0),
    CONTENT("Content", -0.04, 0.22, 0.04, 0.0, 0.0);

    private final String displayName;
    private final double workModifier;
    private final double happinessModifier;
    private final double loyaltyModifier;
    private final double survivalModifier;
    private final double learningModifier;

    Trait(String displayName, double workModifier, double happinessModifier,
          double loyaltyModifier, double survivalModifier, double learningModifier) {
        this.displayName = displayName;
        this.workModifier = workModifier;
        this.happinessModifier = happinessModifier;
        this.loyaltyModifier = loyaltyModifier;
        this.survivalModifier = survivalModifier;
        this.learningModifier = learningModifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getWorkModifier() {
        return workModifier;
    }

    public double getHappinessModifier() {
        return happinessModifier;
    }

    public double getLoyaltyModifier() {
        return loyaltyModifier;
    }

    public double getSurvivalModifier() {
        return survivalModifier;
    }

    public double getLearningModifier() {
        return learningModifier;
    }
}
