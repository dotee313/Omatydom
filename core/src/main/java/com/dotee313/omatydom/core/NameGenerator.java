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

    private static final String[] HUMAN_GIVEN = {
            "Aldric", "Brynn", "Cedric", "Elowen", "Fenn", "Garrick", "Helena", "Joran",
            "Kira", "Marek", "Nia", "Orin", "Petra", "Rowan", "Soren", "Talia"
    };

    private static final String[] HUMAN_FAMILY = {
            "Ashbourne", "Crownhart", "Dawnfield", "Duskhaven", "Embermoor", "Frostvale",
            "Gildhollow", "Hollowcrest", "Ironvale", "Moonmere", "Ravenholt", "Stonebridge",
            "Stormwatch", "Thornbury", "Wyrmford", "Elderbrook"
    };

    private static final String[] PLACE_PREFIX = {
            "Ember", "Hollow", "Crown", "Silent", "Ancient", "Iron", "Moon", "Ash",
            "Dawn", "Storm", "Thorn", "Raven", "Stone", "Frost", "Gilded", "Wyrm",
            "Hearth", "Oak", "Dusk", "Vale"
    };

    private static final String[] CITY_SUFFIX = {
            "hold", "reach", "ford", "haven", "watch", "dale", "port", "mere",
            "crest", "glen", "spire", "moor"
    };

    private static final String[] KINGDOM_SUFFIX = {
            "Reach", "Crown", "Realm", "March", "Dominion", "Empire", "Keep", "Lands",
            "Sway", "Throne"
    };

    private static final String[] CLAN = {
            "Ashkin", "Dawnward", "Emberborn", "Frosthearth", "Ironbark", "Moonclan",
            "Ravenhold", "Stonecall", "Thornspear", "Wyrmkin"
    };

    private static final String[] CULTURE = {
            "Amber Rite", "Ash Faith", "Dawn Song", "Green Oath", "Hollow Code",
            "Iron Cant", "Moon Creed", "Old Vow", "Storm Litany", "Wyrm Lore"
    };

    private static final String[] WEAPON = {
            "Ashfang", "Crownbreaker", "Dawncleaver", "Frostnail", "Hollowedge",
            "Ironbrand", "Moonblade", "Stormpike", "Thornsong", "Wyrmbiter"
    };

    private static final String[] WAR = {
            "Ashen March", "Crownfall", "Hollow Crusade", "Iron Tide", "Long Dawn",
            "Moon Siege", "Shattered Crown", "Storm Tide", "Thorn Rebellion", "Wyrm War"
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

    /** A personal name from the single human word pool; the species key is kept for callers. */
    public static String personName(Random random, String species) {
        return pick(HUMAN_GIVEN, random) + " " + pick(HUMAN_FAMILY, random);
    }

    public static String cityName(Random random) {
        return pick(PLACE_PREFIX, random) + pick(CITY_SUFFIX, random);
    }

    public static String kingdomName(Random random) {
        return pick(PLACE_PREFIX, random) + " " + pick(KINGDOM_SUFFIX, random);
    }

    public static String clanName(Random random) {
        return pick(CLAN, random);
    }

    public static String cultureName(Random random) {
        return pick(CULTURE, random);
    }

    public static String weaponName(Random random) {
        return pick(WEAPON, random);
    }

    public static String warName(Random random) {
        return pick(WAR, random);
    }

    private static String pick(String[] values, Random random) {
        return values[random.nextInt(values.length)];
    }
}
