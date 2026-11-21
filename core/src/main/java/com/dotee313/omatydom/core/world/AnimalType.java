package com.dotee313.omatydom.core.world;

/** Wildlife that can be hunted, fished — or feared. */
public enum AnimalType {
    DEER("Deer", 0xFF8D6E63, false, false),
    BOAR("Boar", 0xFF6D4C41, false, false),
    FISH("Fish", 0xFF4FC3F7, true, false),
    WOLF("Wolf", 0xFF78909C, false, true),
    WORM("Sand Worm", 0xFFD69C5A, false, true),
    GOLEM("Crystal Golem", 0xFFB39DDB, false, true),
    ZOMBIE("Restless One", 0xFF81C784, false, true),
    DRAGON("Young Dragon", 0xFFEF5350, false, true);

    private final String displayName;
    private final int color;
    private final boolean water;
    private final boolean dangerous;

    AnimalType(String displayName, int color, boolean water, boolean dangerous) {
        this.displayName = displayName;
        this.color = color;
        this.water = water;
        this.dangerous = dangerous;
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

    public boolean isDangerous() {
        return dangerous;
    }
}
