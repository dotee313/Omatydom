package com.dotee313.omatydom.core.world;

/** Buildings that builders and the player can raise on the island. */
public enum Structure {
    NONE("Empty", ' ', 0x00000000, 0, 0),
    HUT("Hut", 'H', 0xFFB26A3B, 10, 4),
    HALL("Clan Hall", 'L', 0xFFA1887F, 22, 16),
    TEMPLE("Temple", 'P', 0xFFE0C060, 25, 20),
    LIBRARY("Library", 'B', 0xFFCE93D8, 28, 24),
    TOWER("Watchtower", 'W', 0xFF8D8D8D, 18, 14),
    BARRACKS("Barracks", 'A', 0xFFE57373, 26, 22),
    DOCK("Dock", 'D', 0xFF9C6B3F, 12, 6),
    BRIDGE("Bridge", 'R', 0xFFA1887F, 8, 12),
    MINE("Mine", 'M', 0xFF5A4632, 8, 10),
    SMITHY("Smithy", 'S', 0xFF78909C, 16, 18),
    FARM("Farm", 'F', 0xFF9CCC65, 12, 3),
    GRANARY("Granary", 'G', 0xFFD7A86E, 18, 12),
    WINDMILL("Windmill", 'I', 0xFFE0E0E0, 22, 18),
    WELL("Well", 'O', 0xFF4FC3F7, 8, 14),
    MARKET("Market", 'K', 0xFFFFB74D, 20, 12),
    ROAD("Road", '=', 0xFF9E9E9E, 1, 2),
    GARDEN("Garden", 'N', 0xFF81C784, 8, 4),
    WAREHOUSE("Warehouse", 'U', 0xFFB0BEC5, 20, 18),
    PALACE("Palace", 'V', 0xFFFFD54F, 45, 50);

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
