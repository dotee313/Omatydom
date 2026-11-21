package com.dotee313.omatydom.core.civ;

/** Temporary conditions with their daily work, health and mood effects. */
public enum StatusEffect {
    ASH_FEVER("Ash Fever", "حمى الرماد", 5, 0.70, -2.0, -12.0),
    BURNING("Burning", "احتراق", 3, 0.60, -3.5, -18.0),
    CAFFEINATED("Caffeinated", "منبه بالكافيين", 2, 1.35, 0.2, 10.0),
    COUGH("Cough", "سعال", 4, 0.80, -1.0, -6.0),
    ENCHANTED("Enchanted", "مسحور", 6, 1.15, 0.5, 12.0),
    FROZEN("Frozen", "متجمد", 4, 0.45, -0.8, -10.0),
    INVINCIBLE("Invincible", "لا يُقهر", 3, 1.20, 1.0, 15.0),
    POISONED("Poisoned", "مسموم", 6, 0.65, -2.5, -14.0),
    POWERUP("Powerup", "تعزيز", 4, 1.40, 0.0, -4.0),
    RAGE("Rage", "غضب", 5, 1.30, -0.5, -15.0),
    SHIELD("Shield", "درع", 6, 1.00, 1.5, 8.0),
    SLOWNESS("Slowness", "بطء", 5, 0.55, 0.0, -8.0);

    private final String displayName;
    private final String displayNameAr;
    private final int durationDays;
    private final double workModifier;
    private final double healthPerDay;
    private final double moodModifier;

    StatusEffect(String displayName, String displayNameAr, int durationDays,
                 double workModifier, double healthPerDay, double moodModifier) {
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.durationDays = durationDays;
        this.workModifier = workModifier;
        this.healthPerDay = healthPerDay;
        this.moodModifier = moodModifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNameAr() {
        return displayNameAr;
    }

    /** Alias for {@link #getDisplayNameAr()}. */
    public String getArabicDisplayName() {
        return displayNameAr;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public double getWorkModifier() {
        return workModifier;
    }

    public double getHealthPerDay() {
        return healthPerDay;
    }

    public double getMoodModifier() {
        return moodModifier;
    }
}
