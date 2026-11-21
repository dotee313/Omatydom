package com.dotee313.omatydom.core;

/** Selectable ruleset packs that tune birth rate, work output and trait frequency. */
public enum ModPack {
    CORE("Core", "الأساس", "The standard Omatydom ruleset.", 1.0, 1.0, 1.0),
    TEST_MOD("Test Mod", "نسخة الاختبار", "An experimental ruleset with faster growth.", 1.15, 1.10, 1.0),
    CRAZY_TRAITS("Crazy Traits", "سمات جنونية", "A chaotic ruleset where unusual traits flourish.", 1.0, 0.95, 3.0);

    private final String displayName;
    private final String displayNameAr;
    private final String description;
    private final double birthModifier;
    private final double workModifier;
    private final double traitChanceModifier;

    private ModPack(String displayName, String displayNameAr, String description,
                    double birthModifier, double workModifier, double traitChanceModifier) {
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.description = description;
        this.birthModifier = birthModifier;
        this.workModifier = workModifier;
        this.traitChanceModifier = traitChanceModifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNameAr() {
        return displayNameAr;
    }

    public String getArabicDisplayName() {
        return displayNameAr;
    }

    public String getDescription() {
        return description;
    }

    public double getBirthModifier() {
        return birthModifier;
    }

    public double getWorkModifier() {
        return workModifier;
    }

    public double getTraitChanceModifier() {
        return traitChanceModifier;
    }

    public static ModPack fromKey(String key) {
        if (key == null) {
            return CORE;
        }
        String normalized = key.trim().replace('_', ' ').replace('-', ' ');
        String compact = normalized.replace(" ", "");
        for (ModPack pack : values()) {
            if (pack.name().replace('_', ' ').equalsIgnoreCase(normalized)
                    || pack.name().replace("_", "").equalsIgnoreCase(compact)
                    || pack.displayName.equalsIgnoreCase(key.trim())) {
                return pack;
            }
        }
        return CORE;
    }
}
