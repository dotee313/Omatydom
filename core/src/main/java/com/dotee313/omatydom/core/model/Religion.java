package com.dotee313.omatydom.core.model;

/** Faiths followed on the island. They are labels with social consequences, not truth claims. */
public enum Religion {
    SOLARISM("Solarism", "Worship of the sun and the rhythm of the seasons."),
    ANCESTOR_WORSHIP("Ancestor Worship", "The dead watch over the living."),
    NATURE_CULT("Nature Cult", "The island itself is sacred."),
    THE_ONE("The One", "A single, demanding, universal god."),
    NONE("Unaffiliated", "No formal faith.");

    private final String displayName;
    private final String description;

    Religion(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
