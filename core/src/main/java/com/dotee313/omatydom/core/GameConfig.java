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
    public static final double BIRTH_RATE_PER_CITIZEN = 0.003;

    public static final String RULER_TITLE = "Leader of Omatydom";

    public static final int MAX_DECREES_PER_TICK = 1;

    private GameConfig() {
        // utility class
    }
}
