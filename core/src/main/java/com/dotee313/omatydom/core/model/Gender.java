package com.dotee313.omatydom.core.model;

/** Gender of a citizen. Kept simple for the first playable version. */
public enum Gender {
    MALE("Male"),
    FEMALE("Female");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
