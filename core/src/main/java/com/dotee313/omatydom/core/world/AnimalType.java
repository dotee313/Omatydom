package com.dotee313.omatydom.core.world;

/** Wildlife that can be hunted or fished. */
public enum AnimalType {
    DEER("Deer", 0xFF8D6E63, false),
    BOAR("Boar", 0xFF6D4C41, false),
    FISH("Fish", 0xFF4FC3F7, true);

    private final String displayName;
    private final int color;
    private final boolean water;

    AnimalType(String displayName, int color, boolean water) {
        this.displayName = displayName;
        this.color = color;
        this.water = water;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public boolean isWater() {
        return water;
    }
}
