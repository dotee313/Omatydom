package com.dotee313.omatydom.core.model;

/** The island's social strata. Rank 0 is lowest, 4 is highest. */
public enum SocialClass {
    CHILD("Child", 0, 0xFFBDBDBD),
    LABORER("Laborer", 1, 0xFF9E9E9E),
    ARTISAN("Artisan", 2, 0xFF7E57C2),
    CLERGY("Clergy", 3, 0xFF5C6BC0),
    NOBLE("Noble", 4, 0xFFFFCA28);

    private final String displayName;
    private final int rank;
    private final int color;

    SocialClass(String displayName, int rank, int color) {
        this.displayName = displayName;
        this.rank = rank;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRank() {
        return rank;
    }

    public int getColor() {
        return color;
    }
}
