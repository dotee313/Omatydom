package com.dotee313.omatydom.core.world;

/** Terrain of the island. Colours are plain ARGB ints so Android can use them directly. */
public enum TileType {
    WATER("Water", '~', 0xFF4F86C6, false),
    SAND("Sand", '.', 0xFFDCC48A, true),
    GRASS("Grass", ',', 0xFF79A84B, true),
    FOREST("Forest", 'T', 0xFF2E7D32, true),
    ORCHARD("Orchard", 'F', 0xFF57A639, true),
    MOUNTAIN("Mountain", '^', 0xFF7B7B7B, true),
    CAVE("Cave", 'C', 0xFF4A3B2A, true);

    private final String displayName;
    private final char glyph;
    private final int color;
    private final boolean walkable;

    TileType(String displayName, char glyph, int color, boolean walkable) {
        this.displayName = displayName;
        this.glyph = glyph;
        this.color = color;
        this.walkable = walkable;
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

    public boolean isWalkable() {
        return walkable;
    }
}
