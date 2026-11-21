package com.dotee313.omatydom.core.model;

/** Political ideas held by citizens. They influence loyalty and the chance of unrest. */
public enum Ideology {
    MONARCHIST("Monarchist", "A strong ruler is the natural order."),
    DEMOCRAT("Democrat", "The island should govern itself."),
    THEOCRAT("Theocrat", "Faith and state must be one."),
    TRADITIONALIST("Traditionalist", "Change is a danger."),
    EGALITARIAN("Egalitarian", "Wealth should be shared."),
    NATIONALIST("Nationalist", "The island comes first."),
    APATHETIC("Apathetic", "Politics is not my problem.");

    private final String displayName;
    private final String description;

    Ideology(String displayName, String description) {
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
