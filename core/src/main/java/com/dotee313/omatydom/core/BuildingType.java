package com.dotee313.omatydom.core;

/** The full catalog of structures an island can raise, from a reed hut to a grand cathedral. */
public enum BuildingType {
    // Housing and halls (20)
    HUT("Hut", "كوخ", BuildingCategory.HOUSING, 12, 0, 0, 1),
    LONGHOUSE("Longhouse", "بيت طويل", BuildingCategory.HOUSING, 60, 20, 0, 2),
    HALL("Hall", "قاعة", BuildingCategory.HOUSING, 90, 55, 20, 3),
    PALACE("Palace", "قصر", BuildingCategory.HOUSING, 260, 240, 300, 5),
    REED_SHACK("Reed Shack", "كوخ قصب", BuildingCategory.HOUSING, 10, 0, 0, 1),
    CLAY_COTTAGE("Clay Cottage", "كوخ طيني", BuildingCategory.HOUSING, 18, 12, 0, 1),
    TIMBER_HOME("Timber Home", "بيت خشبي", BuildingCategory.HOUSING, 40, 15, 0, 2),
    PLASTER_HOUSE("Plaster House", "بيت جصي", BuildingCategory.HOUSING, 65, 45, 15, 3),
    STONE_MANOR("Stone Manor", "قصر حجري", BuildingCategory.HOUSING, 110, 120, 70, 4),
    COURTYARD_HOME("Courtyard Home", "بيت بفناء", BuildingCategory.HOUSING, 35, 25, 0, 2),
    HILLSIDE_DWELLING("Hillside Dwelling", "مسكن جبلي", BuildingCategory.HOUSING, 30, 30, 0, 2),
    RIVERSIDE_HOME("Riverside Home", "بيت ضفافي", BuildingCategory.HOUSING, 38, 18, 0, 2),
    CAVE_SHELTER("Cave Shelter", "ملجأ كهفي", BuildingCategory.HOUSING, 8, 6, 0, 1),
    CANOPY_HUT("Canopy Hut", "كوخ مظلل", BuildingCategory.HOUSING, 16, 0, 0, 1),
    TWIN_ROOF_HOUSE("Twin-Roof House", "بيت ذو سقفين", BuildingCategory.HOUSING, 70, 40, 25, 3),
    CLAN_LODGE("Clan Lodge", "نزل العشيرة", BuildingCategory.HOUSING, 75, 35, 20, 3),
    MERCHANT_HOUSE("Merchant House", "بيت تاجر", BuildingCategory.HOUSING, 80, 50, 35, 3),
    ELDER_RESIDENCE("Elder Residence", "مسكن الأكابر", BuildingCategory.HOUSING, 120, 100, 80, 4),
    GATEHOUSE("Gatehouse", "بيت البوابة", BuildingCategory.HOUSING, 100, 110, 60, 4),
    CITADEL_KEEP("Citadel Keep", "برج القلعة", BuildingCategory.HOUSING, 220, 250, 260, 5),

    // Farms and food (20)
    FARM("Farm", "مزرعة", BuildingCategory.FOOD, 20, 0, 0, 1),
    GRANARY("Granary", "مخزن حبوب", BuildingCategory.FOOD, 45, 20, 0, 2),
    WINDMILL("Windmill", "طاحونة ريح", BuildingCategory.FOOD, 75, 55, 20, 3),
    ORCHARD("Orchard", "بستان", BuildingCategory.FOOD, 25, 0, 0, 2),
    VINEYARD("Vineyard", "كرم", BuildingCategory.FOOD, 30, 5, 0, 2),
    FISHING_HUT("Fishing Hut", "كوخ صيد", BuildingCategory.FOOD, 18, 0, 0, 1),
    SALT_PAN("Salt Pan", "ملاحة", BuildingCategory.FOOD, 22, 10, 0, 2),
    BAKERY("Bakery", "مخبز", BuildingCategory.FOOD, 35, 20, 5, 2),
    DAIRY("Dairy", "مزارع الألبان", BuildingCategory.FOOD, 28, 12, 0, 2),
    PIGSTY("Pigsty", "حظيرة خنازير", BuildingCategory.FOOD, 14, 0, 0, 1),
    COOP("Coop", "حظيرة دجاج", BuildingCategory.FOOD, 12, 0, 0, 1),
    APIARY("Apiary", "منحل", BuildingCategory.FOOD, 20, 5, 0, 2),
    RICE_PADDY("Rice Paddy", "حقل أرز", BuildingCategory.FOOD, 32, 8, 0, 2),
    TERRACE_FARM("Terrace Farm", "مزرعة مدرجات", BuildingCategory.FOOD, 60, 65, 25, 3),
    HERB_GARDEN("Herb Garden", "حديقة أعشاب", BuildingCategory.FOOD, 15, 0, 0, 1),
    MUSHROOM_CAVE("Mushroom Cave", "كهف فطر", BuildingCategory.FOOD, 20, 25, 0, 2),
    SMOKEHOUSE("Smokehouse", "مدخنة لحوم", BuildingCategory.FOOD, 55, 35, 15, 3),
    PRESS_HOUSE("Press House", "معصرة", BuildingCategory.FOOD, 60, 30, 20, 3),
    ICE_HOUSE("Ice House", "بيت ثلج", BuildingCategory.FOOD, 50, 45, 20, 3),
    ROYAL_BARN("Royal Barn", "إسطبل ملكي", BuildingCategory.FOOD, 130, 95, 90, 4),

    // Mines and crafts (20)
    MINE("Mine", "منجم", BuildingCategory.CRAFT, 25, 10, 0, 1),
    SMITHY("Smithy", "حدّاد", BuildingCategory.CRAFT, 40, 25, 0, 2),
    QUARRY("Quarry", "مقلع", BuildingCategory.CRAFT, 20, 0, 0, 1),
    SAWMILL("Sawmill", "منشار", BuildingCategory.CRAFT, 45, 20, 5, 2),
    POTTERY("Pottery", "فخار", BuildingCategory.CRAFT, 16, 8, 0, 1),
    WEAVING_SHED("Weaving Shed", "ورشة نسج", BuildingCategory.CRAFT, 30, 10, 0, 2),
    TANNERY("Tannery", "دباغة", BuildingCategory.CRAFT, 32, 15, 0, 2),
    CANDLE_WORKSHOP("Candle Workshop", "ورشة شموع", BuildingCategory.CRAFT, 28, 12, 5, 2),
    JEWELER("Jeweler", "صائغ", BuildingCategory.CRAFT, 65, 40, 35, 3),
    GLASS_FURNACE("Glass Furnace", "فرن زجاج", BuildingCategory.CRAFT, 70, 60, 30, 3),
    BRICK_KILN("Brick Kiln", "فرن طوب", BuildingCategory.CRAFT, 30, 35, 0, 2),
    CHARCOAL_KILN("Charcoal Kiln", "فرن فحم", BuildingCategory.CRAFT, 24, 18, 0, 2),
    PAPER_MILL("Paper Mill", "مصنع ورق", BuildingCategory.CRAFT, 80, 50, 30, 3),
    DYE_WORKS("Dye Works", "مصبغة", BuildingCategory.CRAFT, 34, 16, 5, 2),
    ARMORY("Armory", "مخزن أسلحة", BuildingCategory.CRAFT, 85, 70, 40, 3),
    ALCHEMY_LAB("Alchemy Lab", "مختبر كيمياء", BuildingCategory.CRAFT, 120, 90, 110, 4),
    STEEL_FORGE("Steel Forge", "فرن فولاذ", BuildingCategory.CRAFT, 130, 120, 90, 4),
    CRYSTAL_CUTTER("Crystal Cutter", "قاطع بلور", BuildingCategory.CRAFT, 110, 100, 100, 4),
    MYTHRIL_FORGE("Mythril Forge", "فرن ميثريل", BuildingCategory.CRAFT, 200, 210, 280, 5),
    MASTER_WORKSHOP("Master Workshop", "ورشة الرئيس", BuildingCategory.CRAFT, 190, 180, 240, 5),

    // Military (20)
    BARRACKS("Barracks", "ثكنات", BuildingCategory.MILITARY, 55, 40, 10, 2),
    WATCHTOWER("Watchtower", "برج مراقبة", BuildingCategory.MILITARY, 45, 50, 0, 2),
    WALL_SEGMENT("Wall Segment", "قطعة سور", BuildingCategory.MILITARY, 15, 30, 0, 2),
    GATE_TOWER("Gate Tower", "برج بوابة", BuildingCategory.MILITARY, 80, 90, 25, 3),
    PALISADE("Palisade", "سياج خشبي", BuildingCategory.MILITARY, 22, 0, 0, 1),
    ARCHER_POST("Archer Post", "موقع رماة", BuildingCategory.MILITARY, 30, 15, 0, 2),
    SPEAR_RACK("Spear Rack", "حامل رماح", BuildingCategory.MILITARY, 12, 0, 0, 1),
    TRAINING_YARD("Training Yard", "ساحة تدريب", BuildingCategory.MILITARY, 40, 20, 5, 2),
    WAR_CAMP("War Camp", "معسكر حرب", BuildingCategory.MILITARY, 75, 45, 30, 3),
    FORTRESS("Fortress", "قلعة", BuildingCategory.MILITARY, 140, 160, 120, 4),
    CITADEL("Citadel", "قلعة عليا", BuildingCategory.MILITARY, 240, 280, 300, 5),
    MOAT("Moat", "خندق", BuildingCategory.MILITARY, 10, 35, 0, 2),
    SIGNAL_BEACON("Signal Beacon", "منارة إشارة", BuildingCategory.MILITARY, 55, 45, 20, 3),
    CATAPULT_PAD("Catapult Pad", "منصة قذائف", BuildingCategory.MILITARY, 120, 80, 90, 4),
    STABLES("Stables", "إسطبل", BuildingCategory.MILITARY, 45, 25, 5, 2),
    WEAPON_VAULT("Weapon Vault", "خزنة أسلحة", BuildingCategory.MILITARY, 100, 110, 85, 4),
    GUARD_POST("Guard Post", "نقطة حراسة", BuildingCategory.MILITARY, 16, 8, 0, 1),
    BATTLEMENT("Battlement", "شرفات سور", BuildingCategory.MILITARY, 60, 75, 25, 3),
    SIEGE_WORKSHOP("Siege Workshop", "ورشة حصار", BuildingCategory.MILITARY, 135, 100, 110, 4),
    WAR_HALL("War Hall", "قاعة حرب", BuildingCategory.MILITARY, 210, 190, 250, 5),

    // Culture and religion (20)
    TEMPLE("Temple", "معبد", BuildingCategory.CULTURE, 85, 95, 35, 3),
    LIBRARY("Library", "مكتبة", BuildingCategory.CULTURE, 80, 60, 30, 3),
    SHRINE("Shrine", "ضريح", BuildingCategory.CULTURE, 14, 12, 0, 1),
    ALTAR("Altar", "مذبح", BuildingCategory.CULTURE, 10, 15, 0, 1),
    MONUMENT("Monument", "نصب تذكاري", BuildingCategory.CULTURE, 70, 100, 40, 3),
    STATUE("Statue", "تمثال", BuildingCategory.CULTURE, 25, 45, 10, 2),
    MURAL_HALL("Mural Hall", "قاعة جداريات", BuildingCategory.CULTURE, 75, 50, 35, 3),
    SCHOOL("School", "مدرسة", BuildingCategory.CULTURE, 60, 40, 20, 3),
    ACADEMY("Academy", "أكاديمية", BuildingCategory.CULTURE, 125, 110, 100, 4),
    OBSERVATORY("Observatory", "مرصد", BuildingCategory.CULTURE, 120, 130, 110, 4),
    AMPHITHEATER("Amphitheater", "مسرح مدرج", BuildingCategory.CULTURE, 115, 120, 95, 4),
    MUSIC_PAVILION("Music Pavilion", "مجنحة موسيقى", BuildingCategory.CULTURE, 65, 35, 25, 3),
    DANCE_GROUND("Dance Ground", "ساحة رقص", BuildingCategory.CULTURE, 18, 5, 0, 1),
    STORY_CIRCLE("Story Circle", "حلقة حكايات", BuildingCategory.CULTURE, 12, 10, 0, 1),
    RELIC_VAULT("Relic Vault", "خزنة ذخائر", BuildingCategory.CULTURE, 110, 105, 115, 4),
    CHAPEL("Chapel", "كنيسة", BuildingCategory.CULTURE, 40, 45, 10, 2),
    ANCESTOR_PLAZA("Ancestor Plaza", "ساحة الأجداد", BuildingCategory.CULTURE, 35, 40, 5, 2),
    FESTIVAL_GROUND("Festival Ground", "ساحة مهرجان", BuildingCategory.CULTURE, 30, 15, 0, 2),
    ROYAL_ARCHIVE("Royal Archive", "أرشيف ملكي", BuildingCategory.CULTURE, 180, 170, 220, 5),
    GRAND_CATHEDRAL("Grand Cathedral", "كاتدرائية كبرى", BuildingCategory.CULTURE, 250, 260, 310, 5),

    // Trade and transport (18)
    MARKET("Market", "سوق", BuildingCategory.TRADE, 40, 15, 5, 2),
    DOCK("Dock", "رصيف", BuildingCategory.TRADE, 45, 20, 5, 2),
    ROAD("Road", "طريق", BuildingCategory.TRADE, 5, 4, 0, 1),
    BRIDGE("Bridge", "جسر", BuildingCategory.TRADE, 50, 55, 10, 2),
    WAREHOUSE("Warehouse", "مستودع", BuildingCategory.TRADE, 70, 45, 20, 3),
    CARAVANSERAI("Caravanserai", "خان قوافل", BuildingCategory.TRADE, 130, 100, 120, 4),
    HARBOUR("Harbour", "ميناء", BuildingCategory.TRADE, 90, 80, 45, 3),
    LIGHTHOUSE("Lighthouse", "منارة", BuildingCategory.TRADE, 110, 140, 90, 4),
    TRADE_POST("Trade Post", "مركز تجاري", BuildingCategory.TRADE, 35, 20, 10, 2),
    MINT("Mint", "دار سك", BuildingCategory.TRADE, 115, 100, 130, 4),
    BANK("Bank", "بنك", BuildingCategory.TRADE, 120, 110, 150, 4),
    FERRY_LANDING("Ferry Landing", "رسوّ عبّارة", BuildingCategory.TRADE, 28, 12, 0, 2),
    CANAL_LOCK("Canal Lock", "قناة هويس", BuildingCategory.TRADE, 65, 70, 25, 3),
    PACK_STATION("Pack Station", "محطة شحن", BuildingCategory.TRADE, 32, 18, 5, 2),
    COURIER_HOUSE("Courier House", "بيت البريد", BuildingCategory.TRADE, 55, 30, 20, 3),
    SHIPYARD("Shipyard", "ترسانة", BuildingCategory.TRADE, 145, 120, 110, 4),
    BAZAAR("Bazaar", "سوق شعبي", BuildingCategory.TRADE, 38, 16, 5, 2),
    CUSTOMS_HOUSE("Customs House", "بيت الجمارك", BuildingCategory.TRADE, 85, 75, 70, 4),

    // Nature, defence and utility (16)
    WELL("Well", "بئر", BuildingCategory.UTILITY, 15, 25, 0, 1),
    GARDEN("Garden", "حديقة", BuildingCategory.UTILITY, 12, 0, 0, 1),
    CISTERN("Cistern", "خزان ماء", BuildingCategory.UTILITY, 30, 40, 0, 2),
    DRAIN_DITCH("Drain Ditch", "قناة تصريف", BuildingCategory.UTILITY, 8, 10, 0, 1),
    COMPOST_PIT("Compost Pit", "حفرة سماد", BuildingCategory.UTILITY, 10, 0, 0, 1),
    LOOKOUT_MOUND("Lookout Mound", "تل مراقبة", BuildingCategory.UTILITY, 18, 20, 0, 1),
    WINDBREAK("Windbreak", "حاجز رياح", BuildingCategory.UTILITY, 14, 0, 0, 1),
    STONE_CIRCLE("Stone Circle", "دائرة حجرية", BuildingCategory.UTILITY, 20, 35, 0, 2),
    SPRING_BATH("Spring Bath", "حمام عين", BuildingCategory.UTILITY, 35, 40, 10, 2),
    GROVE("Grove", "بستان بري", BuildingCategory.UTILITY, 10, 0, 0, 1),
    POND("Pond", "بركة", BuildingCategory.UTILITY, 12, 8, 0, 1),
    GROTTO("Grotto", "كهف صغير", BuildingCategory.UTILITY, 22, 30, 0, 2),
    BORDER_MARKER("Border Marker", "علامة حدود", BuildingCategory.UTILITY, 6, 8, 0, 1),
    FIRE_WATCH("Fire Watch", "برج إطفاء", BuildingCategory.UTILITY, 60, 50, 20, 3),
    GRAVEL_PATH("Gravel Path", "ممر حصى", BuildingCategory.UTILITY, 4, 6, 0, 1),
    RESERVOIR("Reservoir", "خزان كبير", BuildingCategory.UTILITY, 140, 170, 80, 4);

    private final String displayName;
    private final String displayNameAr;
    private final BuildingCategory category;
    private final int woodCost;
    private final int stoneCost;
    private final int goldCost;
    private final int tier;

    private BuildingType(String displayName, String displayNameAr, BuildingCategory category,
                         int woodCost, int stoneCost, int goldCost, int tier) {
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.category = category;
        this.woodCost = woodCost;
        this.stoneCost = stoneCost;
        this.goldCost = goldCost;
        this.tier = tier;
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

    public BuildingCategory getCategory() {
        return category;
    }

    public int getWoodCost() {
        return woodCost;
    }

    public int getStoneCost() {
        return stoneCost;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public int getTier() {
        return tier;
    }

    public static BuildingType[] buildableTier(int maxTier) {
        int count = 0;
        for (BuildingType type : values()) {
            if (type.tier <= maxTier) {
                count++;
            }
        }
        BuildingType[] result = new BuildingType[count];
        int index = 0;
        for (BuildingType type : values()) {
            if (type.tier <= maxTier) {
                result[index++] = type;
            }
        }
        return result;
    }

    public static int count() {
        return values().length;
    }

    /** Broad family of a structure, used for grouping in build menus. */
    public enum BuildingCategory {
        HOUSING("إسكان"),
        FOOD("غذاء"),
        CRAFT("حِرَف"),
        MILITARY("عسكري"),
        CULTURE("ثقافة"),
        TRADE("تجارة"),
        UTILITY("مرافق");

        private final String arabicLabel;

        private BuildingCategory(String arabicLabel) {
            this.arabicLabel = arabicLabel;
        }

        public String getArabicLabel() {
            return arabicLabel;
        }
    }
}
