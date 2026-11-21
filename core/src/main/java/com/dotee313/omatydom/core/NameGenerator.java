package com.dotee313.omatydom.core;

import java.util.Random;

/** Small built-in name pool. Replace with data files when the game grows. */
public final class NameGenerator {

    private static final String[] GIVEN = {
            "Ayla", "Boran", "Celia", "Dorian", "Elowen", "Fenn", "Galen", "Hana",
            "Iri", "Joran", "Kira", "Lysa", "Marek", "Nia", "Orin", "Petra",
            "Quill", "Rhea", "Soren", "Tala", "Ulan", "Vera", "Wren", "Zev"
    };

    private static final String[] FAMILY = {
            "of the Tide", "Ashford", "Brightwater", "Cinder", "Duskvale", "Ember",
            "Fallow", "Greyrock", "Hollow", "Ironwood", "Jaspert", "Kestrel",
            "Lantern", "Moss", "Northwind", "Oakheart", "Pale", "Quarry",
            "Redcliff", "Saltmarsh", "Thorne", "Underhill", "Vane", "Wilder"
    };

    private NameGenerator() {
        // utility class
    }

    public static String givenName(Random random) {
        return GIVEN[random.nextInt(GIVEN.length)];
    }

    public static String familyName(Random random) {
        return FAMILY[random.nextInt(FAMILY.length)];
    }

    public static String fullName(Random random) {
        return givenName(random) + " " + familyName(random);
    }
}
