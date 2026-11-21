package com.dotee313.omatydom.desktop;

import com.dotee313.omatydom.core.Age;
import com.dotee313.omatydom.core.GameConfig;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.civ.Kingdom;
import com.dotee313.omatydom.core.civ.TradeRoute;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Ideology;
import com.dotee313.omatydom.core.model.Race;
import com.dotee313.omatydom.core.model.Religion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * v0.8.0 trade-caravan regression. Written against the public API added by
 * Worker 4: {@code game.getTradeRoutes()}, {@code game.getCaravansCompleted()}
 * and {@code world.getCaravansCompleted()}.
 *
 * <p>Assertions: route snapshots are non-null and bounded, every progress
 * value is finite and inside [0,1], route ids are unique and endpoints are
 * distinct kingdoms, the caravan counter never decreases and actually
 * increases over a long run once at least two kingdoms exist, and the v0.6/v0.7
 * calendar and pregnancy invariants still hold.</p>
 *
 * <p>This class intentionally uses direct compile-time references rather than
 * reflection: it is expected to fail compilation until the Worker 4 API is
 * present. The supervisor should merge Worker 4 first, then run qa/verify_v080.sh.</p>
 */
public final class VerifyV080 {
    private static final long[] SEEDS = {80L, 181L, 282L, 383L, 484L};
    private static final int DAYS_PER_SEED = 900;
    private static final int MAX_ACTIVE_ROUTES = 32;

    private VerifyV080() {
    }

    public static void main(String[] args) {
        try {
            checkCalendarInvariants();
            checkPregnancyInvariants();

            int completedTotal = 0;
            int peakRoutes = 0;
            int observedRoutes = 0;
            for (long seed : SEEDS) {
                RunResult result = runSeed(seed);
                completedTotal += result.completed;
                peakRoutes = Math.max(peakRoutes, result.peakRoutes);
                observedRoutes += result.observedRoutes;
                require(result.completed > 0,
                        "seed " + seed + ": caravansCompleted never increased");
                require(result.sawRoute && result.sawProgress,
                        "seed " + seed + ": no in-flight trade route was observed");
            }
            require(completedTotal > 0, "no caravan was ever delivered");
            require(observedRoutes > 0, "trade route list stayed empty in every seed");

            System.out.println("VerifyV080 PASS seeds=" + SEEDS.length
                    + " daysPerSeed=" + DAYS_PER_SEED
                    + " caravansCompleted=" + completedTotal
                    + " observedRoutes=" + observedRoutes
                    + " peakActiveRoutes=" + peakRoutes);
        } catch (Throwable failure) {
            System.out.println("VerifyV080 FAIL: " + describe(failure));
            System.exit(1);
        }
    }

    private static void checkCalendarInvariants() {
        eq(GameConfig.DAYS_PER_YEAR, 360, "days per year");
        eq(GameConfig.DAYS_PER_SEASON, 90, "days per season");
        eq(GameConfig.DAYS_PER_MONTH, 30, "days per month");
        eq(GameConfig.DAYS_PER_AGE, 720, "days per age");
        eq(GameConfig.DAYS_PER_YEAR * GameConfig.MILLIS_PER_GAME_DAY,
                GameConfig.YEAR_REAL_MILLIS,
                "one game year must equal one real hour at x1");
        eq(GameConfig.YEAR_REAL_MILLIS / GameConfig.DAYS_PER_YEAR, 10_000L,
                "milliseconds per game day");
        for (int day = 1; day <= 8_000; day++) {
            eq(OmatydomGame.monthOf(day), ((day - 1) / 30) % 12 + 1, "month at " + day);
            eq(OmatydomGame.seasonOf(day), ((day - 1) / 90) % 4, "season at " + day);
            eq(OmatydomGame.ageOf(day),
                    Age.values()[((day - 1) / GameConfig.DAYS_PER_AGE) % Age.values().length],
                    "age at " + day);
        }
        eq(OmatydomGame.monthOf(18_430), 3, "month at day 18430");
        eq(OmatydomGame.seasonOf(18_430), 0, "season at day 18430");
    }

    private static void checkPregnancyInvariants() {
        eq(GameConfig.GESTATION_DAYS, 270, "gestation");
        eq(GameConfig.MIN_BIRTH_SPACING_DAYS, 360, "post-birth rest");
        Citizen mother = new Citizen(1, "Trade Regression Mother", Gender.FEMALE,
                Race.VAELI, Religion.NONE, Ideology.APATHETIC,
                20 * GameConfig.DAYS_PER_YEAR);
        mother.conceive(500);
        require(mother.isPregnant() && !mother.isDue(769), "gestation must last 270 days");
        require(mother.isDue(770), "due on day 500 + 270");
        mother.deliver(770);
        require(!mother.canConceive(1_129), "one-year rest must be enforced");
        require(mother.canConceive(1_130), "conception allowed after one year");
    }

    private static RunResult runSeed(long seed) {
        OmatydomGame game = OmatydomGame.create(seed);
        World world = game.getWorld();
        int kingdoms = ensureTwoKingdoms(game, world);
        require(kingdoms >= 2, "seed " + seed + ": need two kingdoms for caravans, got " + kingdoms);

        int start = game.getCaravansCompleted();
        require(start == world.getCaravansCompleted(),
                "seed " + seed + ": game/world caravan counters disagree at start");
        int previous = start;
        int peakRoutes = 0;
        int observedRoutes = 0;
        boolean sawRoute = false;
        boolean sawProgress = false;

        for (int i = 0; i < DAYS_PER_SEED; i++) {
            game.advanceDays(1);
            List<TradeRoute> routes = game.getTradeRoutes();
            require(routes != null, "seed " + seed + ": null trade route list");
            require(routes.size() <= MAX_ACTIVE_ROUTES,
                    "seed " + seed + ": unbounded trade route list " + routes.size());
            peakRoutes = Math.max(peakRoutes, routes.size());
            observedRoutes += routes.size();

            Set<Integer> ids = new HashSet<>();
            for (TradeRoute route : routes) {
                require(route != null, "seed " + seed + ": null trade route");
                double progress = route.getProgress();
                require(Double.isFinite(progress) && progress >= 0.0 && progress <= 1.0,
                        "seed " + seed + ": progress outside [0,1]: " + progress);
                // A route starts at 0.0 on the day it opens, then advances daily.
                if (progress > 0.0) {
                    sawProgress = true;
                }
                require(route.getFromKingdomId() != route.getToKingdomId(),
                        "seed " + seed + ": route joins a kingdom to itself");
                require(route.getId() > 0 && ids.add(route.getId()),
                        "seed " + seed + ": duplicate or invalid route id");
                require(route.getReward() >= 0.0 && route.getGood() != null
                                && !route.getGood().isEmpty(),
                        "seed " + seed + ": invalid route cargo/reward");
                sawRoute = true;
            }

            int completed = game.getCaravansCompleted();
            require(completed == world.getCaravansCompleted(),
                    "seed " + seed + ": game/world caravan counters disagree at day "
                            + world.getDay());
            require(completed >= previous,
                    "seed " + seed + ": caravan counter decreased at day " + world.getDay());
            previous = completed;
        }

        require(previous >= start, "caravan counter must not decrease");
        return new RunResult(previous - start, peakRoutes, observedRoutes, sawRoute, sawProgress);
    }

    /** Public-API setup: spawn distant settlers, then try every adult until a second kingdom rises. */
    private static int ensureTwoKingdoms(OmatydomGame game, World world) {
        if (game.getKingdoms().size() >= 2) {
            return game.getKingdoms().size();
        }
        Random random = new Random(80_080L);
        for (int attempt = 0; attempt < 60 && game.getKingdoms().size() < 2; attempt++) {
            int[] spot = distantWalkableSpot(game, random);
            if (spot != null) {
                game.spawnSettlerAt(spot[0], spot[1]);
            }
        }
        for (Citizen citizen : new ArrayList<>(world.getCitizens())) {
            if (game.getKingdoms().size() >= 2) {
                break;
            }
            if (!citizen.isAlive() || !citizen.isAdult()) {
                continue;
            }
            game.foundKingdom(citizen.getId());
        }
        return game.getKingdoms().size();
    }

    /** Prefer a walkable tile at least ten tiles from every existing capital. */
    private static int[] distantWalkableSpot(OmatydomGame game, Random random) {
        for (int attempt = 0; attempt < 200; attempt++) {
            int[] spot = game.getMap().randomWalkable(random);
            if (spot == null) {
                return null;
            }
            boolean distant = true;
            for (Kingdom kingdom : game.getKingdoms()) {
                int dx = kingdom.getCapitalX() - spot[0];
                int dy = kingdom.getCapitalY() - spot[1];
                if (dx * dx + dy * dy < 100) {
                    distant = false;
                    break;
                }
            }
            if (distant) {
                return spot;
            }
        }
        return game.getMap().randomWalkable(random);
    }

    private static final class RunResult {
        final int completed;
        final int peakRoutes;
        final int observedRoutes;
        final boolean sawRoute;
        final boolean sawProgress;

        RunResult(int completed, int peakRoutes, int observedRoutes,
                  boolean sawRoute, boolean sawProgress) {
            this.completed = completed;
            this.peakRoutes = peakRoutes;
            this.observedRoutes = observedRoutes;
            this.sawRoute = sawRoute;
            this.sawProgress = sawProgress;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void eq(int actual, int expected, String label) {
        require(actual == expected, label + ": expected " + expected + " but was " + actual);
    }

    private static void eq(long actual, long expected, String label) {
        require(actual == expected, label + ": expected " + expected + " but was " + actual);
    }

    private static void eq(Object actual, Object expected, String label) {
        require(expected.equals(actual), label + ": expected " + expected + " but was " + actual);
    }

    private static String describe(Throwable failure) {
        String message = failure.getMessage();
        return failure.getClass().getSimpleName()
                + (message == null || message.isEmpty() ? "" : ": " + message);
    }
}
