package com.dotee313.omatydom.core.world;

/** Toggleable rules of the simulation. Each has a stable key and a default state. */
public enum WorldLaw {
    ANGRY_CIVILIANS("angry_civilians", "سكان غاضبون", false),
    ANIMALS_BABIES("animals_babies", "صغار الحيوانات", true),
    ANIMALS_SPAWN("animals_spawn", "ظهور الحيوانات", true),
    BIOME_OVERGROWTH("biome_overgrowth", "نمو الأحياء الحيوية", true),
    BORDER_STEALING("border_stealing", "سرقة الحدود", false),
    CIV_ARMY("civ_army", "جيش مدني", true),
    CIV_BABIES("civ_babies", "أطفال السكان", true),
    POPULATION_CAP("population_cap", "سقف السكان", false),
    DIPLOMACY("diplomacy", "الدبلوماسية", true),
    DISASTERS_NATURE("disasters_nature", "كوارث طبيعية", true),
    DISASTERS_OTHER("disasters_other", "كوارث أخرى", true),
    EROSION("erosion", "التآكل", true),
    FOREVER_LAVA("forever_lava", "حمم دائمة", false),
    FOREVER_TUMOR("forever_tumor", "ورم دائم", false),
    GROW_GRASS("grow_grass", "نمو العشب", true),
    GROW_MINERALS("grow_minerals", "نمو المعادن", true),
    HUNGER("hunger", "الجوع", true),
    KINGDOM_EXPANSION("kingdom_expansion", "توسع المملكة", true),
    OLD_AGE("old_age", "الشيخوخة", true),
    PEACEFUL_MONSTERS("peaceful_monsters", "وحوش مسالمة", false),
    REBELLIONS("rebellions", "ثورات", true),
    VEGETATION_SEEDS("vegetation_seeds", "بذور النباتات", true),
    FOREVER_PEACE("forever_peace", "سلام دائم", false);

    private final String key;
    private final String arabicLabel;
    private final boolean defaultEnabled;

    WorldLaw(String key, String arabicLabel, boolean defaultEnabled) {
        this.key = key;
        this.arabicLabel = arabicLabel;
        this.defaultEnabled = defaultEnabled;
    }

    public String getKey() {
        return key;
    }

    public String getArabicLabel() {
        return arabicLabel;
    }

    /** Alias for callers that append the locale to the getter name. */
    public String getLabelArabic() {
        return arabicLabel;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public boolean getDefaultEnabled() {
        return defaultEnabled;
    }
}
