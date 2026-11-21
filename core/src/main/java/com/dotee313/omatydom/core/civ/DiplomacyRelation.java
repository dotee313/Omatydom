package com.dotee313.omatydom.core.civ;

/** Standing between two kingdoms, with its effect on opinion. */
public enum DiplomacyRelation {
    PEACE("Peace", "سلام", 0),
    ALLIANCE("Alliance", "تحالف", 35),
    RIVALRY("Rivalry", "عداء", -30),
    WAR("War", "حرب", -60);

    private final String displayName;
    private final String displayNameAr;
    private final int opinionDelta;

    DiplomacyRelation(String displayName, String displayNameAr, int opinionDelta) {
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.opinionDelta = opinionDelta;
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

    public int getOpinionDelta() {
        return opinionDelta;
    }
}
