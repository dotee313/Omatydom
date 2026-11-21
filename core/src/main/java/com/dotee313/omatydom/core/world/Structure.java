package com.dotee313.omatydom.core.world;

/** Buildings that builders can raise on the island. */
public enum Structure {
    NONE("Empty", ' ', 0x00000000, 0, 0),
    HUT("Hut", 'H', 0xFFB26A3B, 10, 4),
    TEMPLE("Temple", 'P', 0xFFE0C060, 25, 20),
    TOWER("Watchtower", 'W', 0xFF8D8D8D, 18, 14),
    DOCK("Dock", 'D', 0xFF9C6B3F, 12, 6),
    MINE("Mine", 'M', 0xFF5A4632, 8, 10);

    private final String displayName;
    private final char glyph;
    private final int color;
    private final int woodCost;
    private final int stoneCost;

    Structure(String displayName, char glyph, int color, int woodCost, int stoneCost) {
        this.displayName = displayName;
        this.glyph = glyph;
        this.color = color;
        this.woodCost = woodCost;
        this.stoneCost = stoneCost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public char getGlyph() {
        return glyph;
    }

    public int getColor() {
        return color;
    }

    public int getWoodCost() {
        return woodCost;
    }

    public int getStoneCost() {
        return stoneCost;
    }

    public boolean isBuildable() {
        return this != NONE;
    }
}
