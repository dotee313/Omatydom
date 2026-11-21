package com.dotee313.omatydom.core;

/** The island statistic that a technology nudges when it is learned. */
public enum TechEffect {
    WORK("Work", "عمل"),
    FOOD("Food", "طعام"),
    WOOD("Wood", "خشب"),
    STONE("Stone", "حجر"),
    ORE("Ore", "خام"),
    GOLD("Gold", "ذهب"),
    MILITARY("Military", "عسكري"),
    BIRTH("Birth", "مواليد"),
    HEALTH("Health", "صحة"),
    LEARNING("Learning", "تعلّم"),
    STORAGE("Storage", "تخزين"),
    BUILD_COST("Build Cost", "تكلفة البناء"),
    LOYALTY("Loyalty", "ولاء"),
    DEFENSE("Defense", "دفاع"),
    NAVY("Navy", "بحرية"),
    TRADE("Trade", "تجارة"),
    CULTURE("Culture", "ثقافة");

    private final String displayName;
    private final String arabicName;

    private TechEffect(String displayName, String arabicName) {
        this.displayName = displayName;
        this.arabicName = arabicName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
