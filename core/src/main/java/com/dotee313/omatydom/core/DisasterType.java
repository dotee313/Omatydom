package com.dotee313.omatydom.core;

/** Original disaster catalogue inspired by the seventeen world-log events in the data pack. */
public enum DisasterType {
    ALIEN_VESSLE("Alien Vessel", "سفينة فضائية", 0xFF66BB6A),
    BANDIT_RAID("Bandit Raid", "غارة قطاع الطرق", 0xFF8D6E63),
    BIOMASS_BLOOM("Biomass Bloom", "زحف الكتلة الحيوية", 0xFF9CCC65),
    DRAGON_FLIGHT("Dragon Flight", "تحليق تنين", 0xFFFF7043),
    EARTHQUAKE("Earthquake", "زلزال", 0xFFA1887F),
    EVIL_MAGE("Evil Mage", "ساحر شرير", 0xFFAB47BC),
    HEATWAVE("Heatwave", "موجة حر", 0xFFFFB300),
    HELLSPAWN("Hellspawn", "زريعة الجحيم", 0xFFE53935),
    METEORITE("Meteorite", "نيزك", 0xFFFFD54F),
    TORNADO("Tornado", "إعصار", 0xFF90A4AE),
    TUMOR_SPREAD("Tumor Spread", "انتشار الورم", 0xFF7CB342),
    NECROMANCER_RISE("Necromancer Rise", "نهوض إحيائي الموتى", 0xFF5E35B1),
    ACID_RAIN("Acid Rain", "مطر حمضي", 0xFFAFB42B),
    ASH_FALL("Ash Fall", "سقوط رماد", 0xFF757575),
    LAVA_FLOW("Lava Flow", "تدفق حمم", 0xFFFF5722),
    PLAGUE_SHIP("Plague Ship", "سفينة الطاعون", 0xFF8BC34A),
    WILD_STAMPEDE("Wild Stampede", "هيجان الوحوش", 0xFFD84315);

    private final String displayName;
    private final String arabicName;
    private final int color;

    DisasterType(String displayName, String arabicName, int color) {
        this.displayName = displayName;
        this.arabicName = arabicName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getArabicName() {
        return arabicName;
    }

    public int getColor() {
        return color;
    }
}
