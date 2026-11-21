package com.dotee313.omatydom.core;

/** Broad branches of the island's tree of knowledge. */
public enum TechCategory {
    CRAFT("Craft", "حِرَف"),
    BUILD("Build", "بناء"),
    FARM("Farm", "زراعة"),
    MINING("Mining", "تعدين"),
    MILITARY("Military", "عسكري"),
    GOVERN("Govern", "حكم"),
    CULTURE("Culture", "ثقافة"),
    TRADE("Trade", "تجارة"),
    LORE("Lore", "معرفة");

    private final String displayName;
    private final String arabicName;

    private TechCategory(String displayName, String arabicName) {
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
