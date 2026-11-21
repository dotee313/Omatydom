package com.dotee313.omatydom.core.civ;

/** The single civilized people that can found kingdoms in Omatydom. */
public enum Species {
    HUMAN("Human", "إنسان", 0xFFD9A066, "human", "timber",
            "Adaptable settlers who build quickly and tire slowly.");

    private final String displayName;
    private final String displayNameAr;
    private final int color;
    private final String namePoolKey;
    private final String buildingStyle;
    private final String description;

    Species(String displayName, String displayNameAr, int color, String namePoolKey,
            String buildingStyle, String description) {
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.color = color;
        this.namePoolKey = namePoolKey;
        this.buildingStyle = buildingStyle;
        this.description = description;
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

    /** Plain ARGB colour used for banners and map markers. */
    public int getColor() {
        return color;
    }

    public String getNamePoolKey() {
        return namePoolKey;
    }

    public String getBuildingStyle() {
        return buildingStyle;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Resolves a species from an enum name or a name-pool key, ignoring case.
     * Unknown or null keys fall back to {@link #HUMAN}.
     */
    public static Species fromKey(String key) {
        if (key == null) {
            return HUMAN;
        }
        String normalized = key.trim().replace('-', '_').replace(' ', '_');
        for (Species species : values()) {
            if (species.name().equalsIgnoreCase(normalized)
                    || species.namePoolKey.equalsIgnoreCase(normalized)) {
                return species;
            }
        }
        return HUMAN;
    }
}
