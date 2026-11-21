package com.dotee313.omatydom.core.world;

/** Broad terrain themes. Modifiers scale the base yields of a tile. */
public enum Biome {
    GRASS("Grass", "عشب", 0xFF79A84B, 1.00, 1.00, 1.00),
    SAVANNA("Savanna", "سافانا", 0xFFC9A24B, 1.05, 0.90, 0.85),
    ENCHANTED("Enchanted", "مسحور", 0xFF9B72E8, 1.15, 1.20, 1.30),
    MUSHROOM("Mushroom", "فطر", 0xFFB45FA8, 0.95, 1.10, 1.25),
    CORRUPTED("Corrupted", "فاسد", 0xFF6B3A86, 0.85, 0.60, 0.70),
    INFERNAL("Infernal", "جحيمي", 0xFFD2432B, 0.80, 0.40, 0.35),
    JUNGLE("Jungle", "غابة استوائية", 0xFF2E8B57, 0.90, 1.25, 1.40),
    SWAMP("Swamp", "مستنقع", 0xFF5F7A46, 0.85, 1.05, 1.20),
    WASTELAND("Wasteland", "أرض قاحلة", 0xFF8A7A66, 0.75, 0.35, 0.30),
    DESERT("Desert", "صحراء", 0xFFE3C477, 0.80, 0.45, 0.40),
    CRYSTAL("Crystal", "بلوري", 0xFF6FD3E8, 1.10, 0.70, 0.60),
    CANDY("Candy", "حلوى", 0xFFF07FB4, 1.05, 1.15, 1.10),
    LEMON("Lemon", "ليمون", 0xFFE8DE4F, 1.05, 1.10, 1.15),
    TUMOR("Tumor", "ورم", 0xFF8F4F6B, 0.70, 0.50, 1.50),
    BIOMASS("Biomass", "كتلة حيوية", 0xFF7FA83C, 0.95, 1.30, 1.60),
    PUMPKIN("Pumpkin", "قرع", 0xFFE2872A, 1.00, 1.15, 1.05),
    CYBERTILE("Cybertile", "رقاقة إلكترونية", 0xFF44B0C6, 1.25, 0.80, 0.50),
    PERMAFROST("Permafrost", "تربة صقيعية", 0xFFAFD8E8, 0.75, 0.50, 0.40),
    SAND("Sand", "رمال", 0xFFDCC48A, 0.90, 0.55, 0.45);

    private final String displayName;
    private final String arabicDisplayName;
    private final int color;
    private final double workModifier;
    private final double foodModifier;
    private final double regrowthModifier;

    Biome(String displayName, String arabicDisplayName, int color,
          double workModifier, double foodModifier, double regrowthModifier) {
        this.displayName = displayName;
        this.arabicDisplayName = arabicDisplayName;
        this.color = color;
        this.workModifier = workModifier;
        this.foodModifier = foodModifier;
        this.regrowthModifier = regrowthModifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getArabicDisplayName() {
        return arabicDisplayName;
    }

    /** Alias kept for callers that append the locale to the getter name. */
    public String getDisplayNameArabic() {
        return arabicDisplayName;
    }

    public int getColor() {
        return color;
    }

    public double getWorkModifier() {
        return workModifier;
    }

    public double getFoodModifier() {
        return foodModifier;
    }

    public double getRegrowthModifier() {
        return regrowthModifier;
    }

    /** Best-fitting biome for a base tile; a null tile behaves like grass. */
    public static Biome of(TileType type) {
        if (type == null) {
            return GRASS;
        }
        switch (type) {
            case WATER:
            case SAND:
                return SAND;
            case GRASS:
                return GRASS;
            case FOREST:
                return JUNGLE;
            case ORCHARD:
                return SAVANNA;
            case MOUNTAIN:
                return CRYSTAL;
            case CAVE:
                return MUSHROOM;
            default:
                return GRASS;
        }
    }
}
