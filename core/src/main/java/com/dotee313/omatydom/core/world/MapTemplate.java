package com.dotee313.omatydom.core.world;

/** Starting island layouts. The shape code is a stable id for saved games. */
public enum MapTemplate {
    ISLANDS("Islands", "جزر", 0, "Many small islands scattered across the sea."),
    CONTINENT("Continent", "قارة", 1, "One large landmass with a rugged interior."),
    CHECKERBOARD("Checkerboard", "رقعة شطرنج", 2, "Alternating blocks of land and water."),
    CHEESE("Cheese", "جبن", 3, "A solid island dotted with holes."),
    DONUT("Donut", "دونات", 4, "A ring of land around a central lake."),
    ANTHILL("Anthill", "قرية نمل", 5, "Tight winding passages under a low mound."),
    VOLCANO("Volcano", "بركان", 6, "A fiery peak at the heart of the island."),
    EMPTY("Empty", "فارغ", 7, "Nothing but open water to begin with."),
    BOX_WORLD("Box World", "عالم الصندوق", 8, "A square basin enclosed by steep walls."),
    PANCAKE("Pancake", "فطيرة", 9, "A broad, flat and very fertile plain."),
    TOAST("Toast", "خبز محمص", 10, "A golden rectangle with crisp edges."),
    LASAGNA("Lasagna", "لازانيا", 11, "Stacked layers of land, rock and lava."),
    CUBICLES("Cubicles", "مكعبات", 12, "Ordered tiles divided by narrow channels."),
    BAD_APPLE("Bad Apple", "تفاحة فاسدة", 13, "A fair shore hiding a rotten core."),
    CHAOS_PEARL("Chaos Pearl", "لؤلؤة الفوضى", 14, "Wild terrain thrown around a bright centre."),
    DORMANT_VOLCANO("Dormant Volcano", "بركان خامد", 15, "A quiet crater waiting to wake up.");

    private final String displayName;
    private final String arabicDisplayName;
    private final int shapeCode;
    private final String description;

    MapTemplate(String displayName, String arabicDisplayName, int shapeCode, String description) {
        this.displayName = displayName;
        this.arabicDisplayName = arabicDisplayName;
        this.shapeCode = shapeCode;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getArabicDisplayName() {
        return arabicDisplayName;
    }

    /** Alias for callers that append the locale to the getter name. */
    public String getDisplayNameArabic() {
        return arabicDisplayName;
    }

    public int getShapeCode() {
        return shapeCode;
    }

    public String getDescription() {
        return description;
    }

    /** Every template is selectable, so this simply lists them in shape-code order. */
    public static MapTemplate[] playable() {
        return values();
    }
}
