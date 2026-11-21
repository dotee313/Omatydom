package com.dotee313.omatydom.core.civ;

/** Milestones a settlement can reach, with the value needed to unlock them. */
public enum Achievement {
    FIRST_BLOOD("First Blood", "أول دم",
            "Win your first fight.", 1),
    BUILDER("Builder", "باني",
            "Raise ten structures.", 10),
    POPULATION_50("Population 50", "خمسون نسمة",
            "Grow the settlement to fifty citizens.", 50),
    LONG_RAIN("Long Rain", "مطر طويل",
            "Endure twenty consecutive days of rain.", 20),
    METEOR_MASTER("Meteor Master", "سيد النيازك",
            "Survive five meteor strikes.", 5),
    PEACEKEEPER("Peacekeeper", "حارس السلام",
            "Keep thirty days without a war.", 30),
    WARLORD("Warlord", "أمير الحرب",
            "Win five wars.", 5),
    SCHOLAR("Scholar", "عالم",
            "Discover ten technologies.", 10),
    CLAN_FOUNDER("Clan Founder", "مؤسس عشيرة",
            "Establish your first clan.", 1),
    KINGDOM_FOUNDER("Kingdom Founder", "مؤسس مملكة",
            "Establish your first kingdom.", 1),
    SURVIVOR("Survivor", "ناجٍ",
            "Keep the settlement alive for a hundred days.", 100),
    CENTURY("Century", "قرن",
            "Guide a kingdom through a hundred years.", 100);

    private final String displayName;
    private final String displayNameAr;
    private final String description;
    private final int target;

    Achievement(String displayName, String displayNameAr, String description, int target) {
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.description = description;
        this.target = target;
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

    public String getDescription() {
        return description;
    }

    public int getTarget() {
        return target;
    }
}
