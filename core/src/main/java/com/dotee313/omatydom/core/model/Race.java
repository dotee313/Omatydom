package com.dotee313.omatydom.core.model;

/** The peoples that share the island. Each has a short cultural flavour. */
public enum Race {
    HUMAN("Human", "Pragmatic and numerous."),
    ELF("Elf", "Long-lived, slow to trust, devoted to nature."),
    DWARF("Dwarf", "Stubborn builders and fierce traditionalists."),
    ORC("Orc", "Proud, communal, and quick to take offence."),
    HALFLING("Halfling", "Cheerful traders who avoid unnecessary conflict.");

    private final String displayName;
    private final String description;

    Race(String displayName, String description) {
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
