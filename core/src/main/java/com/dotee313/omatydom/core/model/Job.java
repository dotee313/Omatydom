package com.dotee313.omatydom.core.model;

import com.dotee313.omatydom.core.world.TileType;

/** What an islander spends their day doing. */
public enum Job {
    RULER("Ruler", 'R', 0xFFFFD54F, null),
    PRIEST("Priest", 'P', 0xFFB39DDB, null),
    BUILDER("Builder", 'B', 0xFFD69C5A, null),
    WOODCUTTER("Woodcutter", 'W', 0xFF8D6E63, TileType.FOREST),
    GATHERER("Gatherer", 'G', 0xFF9CCC65, TileType.ORCHARD),
    MINER("Miner", 'M', 0xFF8D8D8D, TileType.MOUNTAIN),
    HUNTER("Hunter", 'H', 0xFFEF5350, null),
    FISHER("Fisher", 'F', 0xFF4FC3F7, null),
    TRADER("Trader", 'T', 0xFF26A69A, null),
    LABORER("Laborer", 'L', 0xFF9E9E9E, null),
    BLACKSMITH("Blacksmith", 'K', 0xFFB0BEC5, TileType.MOUNTAIN),
    SETTLER("Settler", 'S', 0xFF8BC34A, TileType.GRASS),
    ROAD_BUILDER("Road Builder", 'D', 0xFFA1887F, TileType.GRASS),
    CLEANER("Cleaner", 'C', 0xFF4DD0E1, null),
    FIREMAN("Fireman", 'E', 0xFFFF7043, null),
    ATTACKER("Attacker", 'A', 0xFFE53935, null);

    private final String displayName;
    private final char marker;
    private final int color;
    private final TileType target;

    Job(String displayName, char marker, int color, TileType target) {
        this.displayName = displayName;
        this.marker = marker;
        this.color = color;
        this.target = target;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Single letter used by the ASCII map. */
    public char getMarker() {
        return marker;
    }

    public int getColor() {
        return color;
    }

    /** The terrain this job works on, or null if it works on animals/buildings. */
    public TileType getTarget() {
        return target;
    }
}
