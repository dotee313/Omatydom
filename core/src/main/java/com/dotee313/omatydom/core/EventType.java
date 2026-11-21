package com.dotee313.omatydom.core;

/** Categories used in the island chronicle. */
public enum EventType {
    BIRTH("Birth"),
    DEATH("Death"),
    ECONOMY("Economy"),
    DISASTER("Disaster"),
    FESTIVAL("Festival"),
    CONVERSION("Conversion"),
    IDEOLOGY("Ideology"),
    UNREST("Unrest"),
    DECREE("Decree"),
    SUCCESSION("Succession"),
    COUP("Coup"),
    ASSASSINATION("Assassination"),
    DISCOVERY("Discovery"),
    POLITICS("Politics"),
    POWER("World Power"),
    WEATHER("Weather"),
    AGE("Age"),
    CLAN("Clan"),
    KINGDOM("Kingdom"),
    WAR("War"),
    TECH("Technology"),
    LAW("World Law"),
    ACHIEVEMENT("Achievement"),
    BIOME("Biome");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
