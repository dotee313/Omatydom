package com.dotee313.omatydom.core.civ;

/** Reasons a kingdom may go to war. */
public enum WarType {
    CONQUEST("Conquest", "غزو",
            "A deliberate campaign to seize land and vassals."),
    INSPIRE("Inspire", "إلهام",
            "A crusade fought to spread an ideal rather than take territory."),
    REBELLION("Rebellion", "تمرد",
            "An uprising of subjects against their own ruler."),
    SPITE("Spite", "حقد",
            "A bitter feud fought purely over old insults."),
    WHISPER("Whisper", "همس",
            "A quiet shadow war of rumors, agents and veiled influence.");

    private final String displayName;
    private final String displayNameAr;
    private final String description;

    WarType(String displayName, String displayNameAr, String description) {
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
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

    public String getDescription() {
        return description;
    }
}
