package com.dotee313.omatydom.core;

/** Tunable constants for the simulation. Change these to re-balance the game. */
public final class GameConfig {

    public static final int INITIAL_POPULATION = 24;
    public static final int MAX_POPULATION = 80;

    // Island grid. Small enough to render hundreds of tiles cheaply on a phone.
    public static final int MAP_WIDTH = 40;
    public static final int MAP_HEIGHT = 24;
    public static final int INITIAL_ANIMALS = 22;
    public static final int MAX_ANIMALS = 40;

    // Yields from the different jobs.
    public static final int WOOD_PER_LOG = 4;
    public static final int FOOD_PER_FRUIT = 8;
    public static final int FOOD_PER_FISH = 7;
    public static final int FOOD_PER_HUNT = 12;
    public static final int ORE_PER_DIG = 1;
    public static final int STONE_PER_DIG = 3;

    // Food now comes from the agents' work (gathering, fishing, hunting),
    // while every citizen eats FOOD_PER_CITIZEN every day.
    public static final double FOOD_PER_CITIZEN = 1.0;
    /** Share of the storehouse lost to rot every day. Keeps food bounded. */
    public static final double FOOD_SPOILAGE_RATE = 0.015;
    public static final double TAX_PER_CITIZEN = 0.12;
    /** Cost of roads, guards and clerks per citizen, per day. */
    public static final double ADMIN_COST_PER_CITIZEN = 0.08;

    // Calendar: 12 months of 30 days, four seasons of 90 days.
    // One full game year takes exactly one real hour at speed x1.
    public static final int DAYS_PER_YEAR = 360;
    public static final int DAYS_PER_SEASON = 90;
    public static final int DAYS_PER_MONTH = 30;
    public static final int DAYS_PER_AGE = 2 * DAYS_PER_YEAR;
    public static final long YEAR_REAL_MILLIS = 3_600_000L;
    public static final long MILLIS_PER_GAME_DAY = YEAR_REAL_MILLIS / DAYS_PER_YEAR;
    public static final int UI_TICK_MILLIS = 500;

    // Pregnancy: a woman carries one child for three seasons (270 days),
    // then rests for a full year before she can conceive again.
    public static final int GESTATION_DAYS = 3 * DAYS_PER_SEASON;
    public static final int MIN_BIRTH_SPACING_DAYS = DAYS_PER_YEAR;
    public static final double CONCEPTION_CHANCE_PER_DAY = 0.0014;

    public static final String RULER_TITLE = "Leader of Omatydom";

    public static final int MAX_DECREES_PER_TICK = 1;

    private GameConfig() {
        // utility class
    }
}
