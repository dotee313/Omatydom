package com.dotee313.omatydom.core;

/** Tunable constants for the simulation. Change these to re-balance the game. */
public final class GameConfig {

    public static final int INITIAL_POPULATION = 24;
    public static final int MAX_POPULATION = 200;

    // Production is population * FOOD_PER_WORKER + BASE_FOOD_PRODUCTION,
    // while every citizen eats FOOD_PER_CITIZEN. The small surplus shrinks as
    // the island grows, so famine eventually becomes a real problem.
    public static final double BASE_FOOD_PRODUCTION = 6.0;
    public static final double FOOD_PER_WORKER = 0.9;
    public static final double FOOD_PER_CITIZEN = 1.0;
    /** Share of the storehouse lost to rot every day. Keeps food bounded. */
    public static final double FOOD_SPOILAGE_RATE = 0.015;
    public static final double TAX_PER_CITIZEN = 0.12;
    /** Cost of roads, guards and clerks per citizen, per day. */
    public static final double ADMIN_COST_PER_CITIZEN = 0.08;
    public static final double BIRTH_RATE_PER_CITIZEN = 0.0022;

    public static final String RULER_TITLE = "Leader of Omatydom";

    public static final int MAX_DECREES_PER_TICK = 1;

    private GameConfig() {
        // utility class
    }
}
