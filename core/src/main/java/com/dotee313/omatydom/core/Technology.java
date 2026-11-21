package com.dotee313.omatydom.core;

/** Discoveries passed down through the ages, from hearth lore to star-metal. */
public enum Technology {
    ANCESTRAL_WISDOM("Ancestors' Wisdom", "حكمة الأجداد", TechCategory.LORE, TechEffect.LEARNING, 0.10, 30),
    SHELL_ARMOUR("Shell Armour", "درع الصدفة", TechCategory.MILITARY, TechEffect.DEFENSE, 0.10, 35),
    ARMOUR_SMITH("Armour Smith", "حدّاد الدروع", TechCategory.CRAFT, TechEffect.DEFENSE, 0.12, 60),
    ARMY_DRILL_I("Army Drill I", "تدريب الجيش ١", TechCategory.MILITARY, TechEffect.MILITARY, 0.08, 40),
    ARMY_DRILL_II("Army Drill II", "تدريب الجيش ٢", TechCategory.MILITARY, TechEffect.MILITARY, 0.12, 95),
    ARMY_DRILL_III("Army Drill III", "تدريب الجيش ٣", TechCategory.MILITARY, TechEffect.MILITARY, 0.16, 190),
    BARRACKS("Barracks", "ثكنات", TechCategory.BUILD, TechEffect.MILITARY, 0.10, 55),
    SAILING_CRAFT("Sailing Craft", "قوارب الشراع", TechCategory.BUILD, TechEffect.NAVY, 0.10, 50),
    HARBOUR_DOCKS("Harbour Docks", "أرصفة الميناء", TechCategory.BUILD, TechEffect.NAVY, 0.12, 70),
    SHAFT_MINE("Shaft Mine", "منجم الحفر", TechCategory.BUILD, TechEffect.ORE, 0.10, 45),
    CAUSEWAYS("Causeways", "طرق مرتفعة", TechCategory.BUILD, TechEffect.TRADE, 0.08, 60),
    STONE_EFFIGIES("Stone Effigies", "تماثيل حجرية", TechCategory.CULTURE, TechEffect.CULTURE, 0.10, 80),
    LOOKOUT_POST("Lookout Post", "برج المراقبة", TechCategory.BUILD, TechEffect.DEFENSE, 0.10, 55),
    GRANARY_I("Granary I", "مخزن الحبوب ١", TechCategory.BUILD, TechEffect.STORAGE, 0.12, 40),
    GRANARY_II("Granary II", "مخزن الحبوب ٢", TechCategory.BUILD, TechEffect.STORAGE, 0.18, 100),
    GRANARY_III("Granary III", "مخزن الحبوب ٣", TechCategory.BUILD, TechEffect.STORAGE, 0.25, 210),
    CONVERSION_RITE_I("Conversion Rite I", "طقس التحويل ١", TechCategory.CULTURE, TechEffect.LOYALTY, 0.08, 45),
    CONVERSION_RITE_II("Conversion Rite II", "طقس التحويل ٢", TechCategory.CULTURE, TechEffect.LOYALTY, 0.12, 105),
    CONVERSION_RITE_III("Conversion Rite III", "طقس التحويل ٣", TechCategory.CULTURE, TechEffect.LOYALTY, 0.18, 215),
    CREED_SPREAD_I("Creed Spread I", "نشر العقيدة ١", TechCategory.CULTURE, TechEffect.CULTURE, 0.08, 50),
    CREED_SPREAD_II("Creed Spread II", "نشر العقيدة ٢", TechCategory.CULTURE, TechEffect.CULTURE, 0.12, 110),
    CREED_SPREAD_III("Creed Spread III", "نشر العقيدة ٣", TechCategory.CULTURE, TechEffect.CULTURE, 0.18, 220),
    DEFENSIVE_SCHEMA("Defensive Schema", "مخطط الدفاع", TechCategory.MILITARY, TechEffect.DEFENSE, 0.14, 130),
    GEMWATER("Gemwater", "ماء الجواهر", TechCategory.LORE, TechEffect.HEALTH, 0.08, 90),
    COUNCIL_HALL_I("Council Hall I", "قاعة المجلس ١", TechCategory.GOVERN, TechEffect.LOYALTY, 0.10, 45),
    COUNCIL_HALL_II("Council Hall II", "قاعة المجلس ٢", TechCategory.GOVERN, TechEffect.LOYALTY, 0.14, 110),
    COUNCIL_HALL_III("Council Hall III", "قاعة المجلس ٣", TechCategory.GOVERN, TechEffect.GOLD, 0.18, 225),
    ISLAND_HEROES("Island Heroes", "أبطال الجزيرة", TechCategory.CULTURE, TechEffect.LOYALTY, 0.12, 85),
    HOUSE_TIER_0("Hearth Shelter", "ملجأ الموقد", TechCategory.BUILD, TechEffect.BIRTH, 0.05, 20),
    HOUSE_TIER_1("Wattle House", "بيت القصب", TechCategory.BUILD, TechEffect.HEALTH, 0.06, 35),
    HOUSE_TIER_2("Timber House", "بيت الخشب", TechCategory.BUILD, TechEffect.STORAGE, 0.10, 60),
    HOUSE_TIER_3("Plaster House", "بيت الجص", TechCategory.BUILD, TechEffect.BIRTH, 0.10, 100),
    HOUSE_TIER_4("Stone House", "بيت الحجر", TechCategory.BUILD, TechEffect.HEALTH, 0.12, 160),
    HOUSE_TIER_5("Hall House", "بيت القاعة", TechCategory.BUILD, TechEffect.LOYALTY, 0.15, 260),
    HOMESTEAD_I("Homestead I", "مستوطنة ١", TechCategory.BUILD, TechEffect.BIRTH, 0.06, 40),
    HOMESTEAD_II("Homestead II", "مستوطنة ٢", TechCategory.BUILD, TechEffect.BIRTH, 0.10, 95),
    HOMESTEAD_III("Homestead III", "مستوطنة ٣", TechCategory.BUILD, TechEffect.BIRTH, 0.15, 200),
    LEARNING_I("Learning I", "تعلّم ١", TechCategory.LORE, TechEffect.LEARNING, 0.08, 35),
    LEARNING_II("Learning II", "تعلّم ٢", TechCategory.LORE, TechEffect.LEARNING, 0.12, 90),
    LEARNING_III("Learning III", "تعلّم ٣", TechCategory.LORE, TechEffect.LEARNING, 0.18, 185),
    BRONZE_CRAFT("Bronze Craft", "صناعة البرونز", TechCategory.CRAFT, TechEffect.BUILD_COST, 0.10, 50),
    COPPER_CRAFT("Copper Craft", "صناعة النحاس", TechCategory.CRAFT, TechEffect.BUILD_COST, 0.10, 35),
    IRON_CRAFT("Iron Craft", "صناعة الحديد", TechCategory.CRAFT, TechEffect.MILITARY, 0.12, 120),
    SILVER_CRAFT("Silver Craft", "صناعة الفضة", TechCategory.CRAFT, TechEffect.TRADE, 0.10, 110),
    STEEL_CRAFT("Steel Craft", "صناعة الفولاذ", TechCategory.CRAFT, TechEffect.MILITARY, 0.16, 190),
    ADAMANTINE_CRAFT("Adamantine Craft", "صناعة الأدامانتين", TechCategory.CRAFT, TechEffect.DEFENSE, 0.20, 260),
    MYTHRIL_CRAFT("Mythril Craft", "صناعة الميثريل", TechCategory.CRAFT, TechEffect.DEFENSE, 0.24, 300),
    BATTLE_SCHEMA("Battle Schema", "مخطط القتال", TechCategory.MILITARY, TechEffect.MILITARY, 0.15, 140),
    ORE_YIELDS("Ore Yields", "غلة الخام", TechCategory.MINING, TechEffect.ORE, 0.12, 85),
    NATURE_BONDS("Nature Bonds", "ألفة الطبيعة", TechCategory.CULTURE, TechEffect.FOOD, 0.08, 55),
    KEEN_AXES("Keen Axes", "فؤوس حادة", TechCategory.CRAFT, TechEffect.WOOD, 0.12, 45),
    CAPTIVE_YOUTH("Captive Youth", "شباب أسير", TechCategory.GOVERN, TechEffect.BIRTH, 0.05, 70),
    PLUNDER_RIGHTS("Plunder Rights", "حقوق الغنائم", TechCategory.TRADE, TechEffect.GOLD, 0.10, 75),
    TEMPLE_RITES("Temple Rites", "شعائر المعبد", TechCategory.BUILD, TechEffect.LOYALTY, 0.12, 120),
    MARKET_TRADE("Market Trade", "تجارة السوق", TechCategory.TRADE, TechEffect.TRADE, 0.10, 60),
    TRADE_ROUTES("Trade Routes", "طرق التجارة", TechCategory.TRADE, TechEffect.GOLD, 0.12, 130),
    TOWER_BONUS("Tower Bonus", "مكافأة البرج", TechCategory.MILITARY, TechEffect.DEFENSE, 0.15, 100),
    WAY_OF_LIFE("Way of Life", "طريقة الحياة", TechCategory.LORE, TechEffect.HEALTH, 0.10, 90),
    AXE_CRAFT("Axe Craft", "صناعة الفأس", TechCategory.CRAFT, TechEffect.WOOD, 0.10, 30),
    BOW_CRAFT("Bow Craft", "صناعة القوس", TechCategory.CRAFT, TechEffect.MILITARY, 0.10, 40),
    HAMMER_CRAFT("Hammer Craft", "صناعة المطرقة", TechCategory.CRAFT, TechEffect.BUILD_COST, 0.10, 35),
    SPEAR_CRAFT("Spear Craft", "صناعة الرمح", TechCategory.CRAFT, TechEffect.MILITARY, 0.10, 45),
    SWORD_CRAFT("Sword Craft", "صناعة السيف", TechCategory.CRAFT, TechEffect.MILITARY, 0.12, 85),
    ARMS_PRODUCTION("Arms Production", "إنتاج الأسلحة", TechCategory.CRAFT, TechEffect.MILITARY, 0.15, 150),
    ARMSMITH("Armsmith", "صانع الأسلحة", TechCategory.CRAFT, TechEffect.MILITARY, 0.14, 125),
    WELL_DIGGING("Well Digging", "حفر البئر", TechCategory.BUILD, TechEffect.HEALTH, 0.10, 40),
    WINDMILL_MILLING("Windmill Milling", "طاحونة الرياح", TechCategory.BUILD, TechEffect.FOOD, 0.12, 110),
    ZONE_I("Zone I", "منطقة ١", TechCategory.GOVERN, TechEffect.GOLD, 0.06, 45),
    ZONE_II("Zone II", "منطقة ٢", TechCategory.GOVERN, TechEffect.STORAGE, 0.12, 115),
    ZONE_III("Zone III", "منطقة ٣", TechCategory.GOVERN, TechEffect.BIRTH, 0.15, 235);

    private final String displayName;
    private final String arabicName;
    private final TechCategory category;
    private final TechEffect effect;
    private final double magnitude;
    private final int cost;

    private Technology(String displayName, String arabicName, TechCategory category,
                       TechEffect effect, double magnitude, int cost) {
        this.displayName = displayName;
        this.arabicName = arabicName;
        this.category = category;
        this.effect = effect;
        this.magnitude = magnitude;
        this.cost = cost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getArabicName() {
        return arabicName;
    }

    public TechCategory getCategory() {
        return category;
    }

    public TechEffect getEffect() {
        return effect;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public int getCost() {
        return cost;
    }

    public static int count() {
        return values().length;
    }
}
