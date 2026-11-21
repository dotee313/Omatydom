package com.dotee313.omatydom.core;

/** Every tradeable material on the island, from driftwood to mythril. */
public enum ResourceType {
    WOOD("Wood", "خشب", 0xFF8B5A2B, 2),
    STONE("Stone", "حجر", 0xFF808080, 2),
    GOLD("Gold", "ذهب", 0xFFFFD700, 40),
    IRON("Iron", "حديد", 0xFFB0B0B8, 12),
    COPPER("Copper", "نحاس", 0xFFB87333, 8),
    SILVER("Silver", "فضة", 0xFFC0C0C8, 25),
    ADAMANTINE("Adamantine", "أدامانتين", 0xFF4B0082, 70),
    MYTHRIL("Mythril", "ميثريل", 0xFF77C8E8, 90),
    GEMS("Gems", "جواهر", 0xFF50C878, 55),
    SALT("Salt", "ملح", 0xFFF5F5F0, 5),
    BONES("Bones", "عظام", 0xFFE8E0C8, 3),
    LEATHER("Leather", "جلود", 0xFF8B4513, 7),
    HERBS("Herbs", "أعشاب", 0xFF3CB371, 4),
    FISH("Fish", "سمك", 0xFF5F9EA0, 6),
    MEAT("Meat", "لحم", 0xFFCD5C5C, 8),
    WHEAT("Wheat", "قمح", 0xFFDAA520, 5),
    BREAD("Bread", "خبز", 0xFFD2A679, 9),
    BERRIES("Berries", "توت", 0xFF800020, 4),
    BANANA("Banana", "موز", 0xFFFFE135, 4),
    COCONUT("Coconut", "جوز هند", 0xFFD2B48C, 5),
    LEMONS("Lemons", "ليمون", 0xFFFFFF00, 4),
    PEPPERS("Peppers", "فلفل", 0xFF32CD32, 6),
    MUSHROOMS("Mushrooms", "فطر", 0xFFCD853F, 5),
    COOKED_MEAT("Cooked Meat", "لحم مطبوخ", 0xFFA0522D, 12),
    FLOUR("Flour", "دقيق", 0xFFF5DEB3, 7),
    ORE("Ore", "خام", 0xFF696969, 6),
    CRYSTAL("Crystal", "بلورة", 0xFF00CED1, 35),
    CLAY("Clay", "طين", 0xFFB66A50, 3),
    SAND("Sand", "رمل", 0xFFEDC9AF, 2),
    WATER("Water", "ماء", 0xFF4169E1, 2),
    WOOL("Wool", "صوف", 0xFFFFF8DC, 8),
    DYE("Dye", "صبغة", 0xFF800080, 10),
    PAPER("Paper", "ورق", 0xFFFDFDF6, 9);

    private final String displayName;
    private final String displayNameAr;
    private final int color;
    private final int baseValue;

    private ResourceType(String displayName, String displayNameAr, int color, int baseValue) {
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.color = color;
        this.baseValue = baseValue;
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

    public int getColor() {
        return color;
    }

    public int getBaseValue() {
        return baseValue;
    }

    public static ResourceType[] foodTypes() {
        return new ResourceType[] {
                BERRIES, BANANA, COCONUT, LEMONS, MUSHROOMS, FISH,
                MEAT, BREAD, WHEAT, HERBS, COOKED_MEAT, FLOUR
        };
    }

    public static ResourceType[] metalTypes() {
        return new ResourceType[] { IRON, COPPER, SILVER, GOLD, ADAMANTINE, MYTHRIL, ORE };
    }
}
