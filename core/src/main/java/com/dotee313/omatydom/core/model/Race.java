package com.dotee313.omatydom.core.model;

/** Original peoples of Omatydom — none of them exist in our world. */
public enum Race {
    VAELI("Vaeli", "Leaf-skinned forest wanderers who hear the trees.", 0xFF66BB6A, Job.WOODCUTTER),
    KORUN("Korun", "Stone-hearted highlanders with crystal veins.", 0xFFA0A5AB, Job.MINER),
    SIRENI("Sireni", "Tide-born people with silver gills and calm voices.", 0xFF4FC3F7, Job.FISHER),
    DRAVI("Dravi", "Cave clans who see in the dark and gather rare roots.", 0xFFAB47BC, Job.GATHERER),
    LUMIN("Lumin", "Sun-touched wanderers who keep the old songs.", 0xFFFFD54F, Job.PRIEST),
    TIREN("Tiren", "Clay-shapers who raise houses from mud and timber.", 0xFFD69C5A, Job.BUILDER);

    private final String displayName;
    private final String description;
    private final int color;
    private final Job preferredJob;

    Race(String displayName, String description, int color, Job preferredJob) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
        this.preferredJob = preferredJob;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /** Plain ARGB colour used for the tiny on-screen bodies. */
    public int getColor() {
        return color;
    }

    public Job getPreferredJob() {
        return preferredJob;
    }
}
