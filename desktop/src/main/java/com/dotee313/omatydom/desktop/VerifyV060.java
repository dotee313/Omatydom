package com.dotee313.omatydom.desktop;

import com.dotee313.omatydom.core.Age;
import com.dotee313.omatydom.core.GameConfig;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Ideology;
import com.dotee313.omatydom.core.model.Race;
import com.dotee313.omatydom.core.model.Religion;

import java.util.HashMap;
import java.util.Map;

/**
 * Deterministic v0.6.0 regression, recreated after the original VerifyV060 was
 * dropped from the repository.
 *
 * <p>Covers: 25 seeded islands advanced to a combined 18,430 world-days
 * (~736 days per world), no simulation exceptions, monotonic day counters,
 * birth-counter/delivery consistency, and - most importantly - the exact
 * 270-day gestation plus 360-day (one full year) rest rule. Only public core
 * APIs are used.</p>
 *
 * <p>Note: the old verifier's claim that every pregnant woman is always
 * strictly less than 270 days pregnant cannot hold in the current engine when
 * the population cap is reached (stepBirths returns early and delivery waits).
 * The closest safe check is implemented here: day-by-day delivery tracking
 * with the exact due day, plus a direct Citizen-level assertion of the
 * 270/360-day rule using only public methods.</p>
 */
public final class VerifyV060 {
    private static final int SEED_COUNT = 25;
    private static final int TOTAL_WORLD_DAYS = 18_430;
    private static final int BASE_DAYS = (TOTAL_WORLD_DAYS - SEED_COUNT) / SEED_COUNT;
    private static final int EXTRA_DAYS = (TOTAL_WORLD_DAYS - SEED_COUNT) % SEED_COUNT;

    private VerifyV060() {
    }

    public static void main(String[] args) {
        try {
            require(BASE_DAYS == 736 && EXTRA_DAYS == 5, "unexpected day budget");
            checkCalendarConstants();
            checkPregnancyRuleOnCitizen();

            int totalBirths = 0;
            int totalDeliveries = 0;
            int worldDaySum = 0;
            int simulatedDays = 0;

            for (int i = 0; i < SEED_COUNT; i++) {
                // Five seeds get one extra day so that 25 * 1 + 18,405
                // advances = exactly 18,430 world-days.
                int days = BASE_DAYS + (i < EXTRA_DAYS ? 1 : 0);
                long seed = 6_000L + i * 7L;
                SeedResult result = simulate(seed, days);
                totalBirths += result.births;
                totalDeliveries += result.deliveries;
                worldDaySum += result.finalDay;
                simulatedDays += days;
            }

            require(worldDaySum == TOTAL_WORLD_DAYS,
                    "world-day total " + worldDaySum + " != " + TOTAL_WORLD_DAYS);
            require(totalBirths > 0, "no births were produced by the long run");
            require(totalDeliveries == totalBirths,
                    "deliveries " + totalDeliveries + " != births " + totalBirths);
            System.out.println("VerifyV060 PASS seeds=" + SEED_COUNT
                    + " simulatedDays=" + simulatedDays
                    + " worldDays=" + worldDaySum
                    + " births=" + totalBirths
                    + " deliveries=" + totalDeliveries);
        } catch (Throwable failure) {
            System.out.println("VerifyV060 FAIL: " + describe(failure));
            System.exit(1);
        }
    }

    /** Calendar constants are the foundation of the pregnancy assertions. */
    private static void checkCalendarConstants() {
        eq(GameConfig.DAYS_PER_YEAR, 360, "days per year");
        eq(GameConfig.DAYS_PER_SEASON, 90, "days per season");
        eq(GameConfig.DAYS_PER_MONTH, 30, "days per month");
        eq(GameConfig.DAYS_PER_AGE, 720, "days per age");
        eq(GameConfig.GESTATION_DAYS, 270, "gestation must be three seasons");
        eq(GameConfig.MIN_BIRTH_SPACING_DAYS, 360, "rest must be one full year");
        require(GameConfig.GESTATION_DAYS + GameConfig.MIN_BIRTH_SPACING_DAYS
                == 630, "one child at a time must span 630 days");
        for (int day = 1; day <= 4_000; day++) {
            eq(OmatydomGame.monthOf(day), ((day - 1) / 30) % 12 + 1, "month at day " + day);
            eq(OmatydomGame.seasonOf(day), ((day - 1) / 90) % 4, "season at day " + day);
            eq(OmatydomGame.ageOf(day),
                    Age.values()[((day - 1) / GameConfig.DAYS_PER_AGE) % Age.values().length],
                    "age at day " + day);
        }
    }

    /**
     * Exact public-API check of "270 days pregnant, then exactly one year of
     * rest". This is independent of random engine draws.
     */
    private static void checkPregnancyRuleOnCitizen() {
        Citizen mother = new Citizen(1, "Regression Mother", Gender.FEMALE,
                Race.VAELI, Religion.NONE, Ideology.APATHETIC,
                20 * GameConfig.DAYS_PER_YEAR);
        require(mother.isAdult(), "test mother must be an adult");
        require(mother.canConceive(1_000), "adult woman should be able to conceive");

        mother.conceive(1_000);
        require(mother.isPregnant(), "conceive() must set pregnant");
        eq(mother.getPregnancyStartDay(), 1_000, "pregnancy start day");
        for (int day = 1_000; day < 1_270; day++) {
            require(!mother.isDue(day), "not due at day " + day);
            eq(mother.getGestationDay(day), day - 1_000, "gestation day " + day);
            require(!mother.canConceive(day), "cannot conceive while pregnant at " + day);
        }
        require(mother.isDue(1_270), "must be due on exactly day 1000 + 270");
        eq(mother.getGestationDay(1_270), 270, "gestation clamps to 270");
        require(!mother.canConceive(1_270), "still pregnant on the due day");

        mother.deliver(1_270);
        require(!mother.isPregnant(), "delivery ends pregnancy");
        eq(mother.getPregnancyStartDay(), -1, "pregnancy start cleared");
        eq(mother.getLastBirthDay(), 1_270, "last birth day recorded");
        for (int day = 1_270; day < 1_630; day++) {
            require(!mother.canConceive(day), "one-year rest violated at day " + day);
        }
        require(mother.canConceive(1_630), "can conceive after exactly 360 days");

        Citizen male = new Citizen(2, "Regression Father", Gender.MALE,
                Race.VAELI, Religion.NONE, Ideology.APATHETIC,
                20 * GameConfig.DAYS_PER_YEAR);
        require(!male.canConceive(1_000), "men must not conceive");
        Citizen child = new Citizen(3, "Regression Child", Gender.FEMALE,
                Race.VAELI, Religion.NONE, Ideology.APATHETIC,
                10 * GameConfig.DAYS_PER_YEAR);
        require(!child.canConceive(1_000), "children must not conceive");
    }

    private static SeedResult simulate(long seed, int days) {
        OmatydomGame game = OmatydomGame.create(seed);
        World world = game.getWorld();
        int initialPopulation = world.getPopulation();
        require(initialPopulation == GameConfig.INITIAL_POPULATION,
                "seed " + seed + " initial population");

        Map<Integer, Snapshot> previous = new HashMap<>();
        int previousBirths = world.getBirths();
        int deliveries = 0;

        for (int step = 0; step < days; step++) {
            previous.clear();
            for (Citizen citizen : world.getCitizens()) {
                if (citizen.isAlive()) {
                    previous.put(citizen.getId(), new Snapshot(citizen));
                }
            }

            int dayBefore = world.getDay();
            game.advanceDays(1);
            int day = world.getDay();
            require(day == dayBefore + 1, "seed " + seed + " day did not advance");
            require(world.getPopulation() >= 0
                            && world.getPopulation() <= GameConfig.MAX_POPULATION + 40,
                    "seed " + seed + " population out of range at " + day);
            require(world.getFood() >= 0 && world.getGold() >= 0,
                    "seed " + seed + " negative stockpile at " + day);

            int births = world.getBirths();
            require(births >= previousBirths, "seed " + seed + " births counter went backwards");
            int deliveredToday = 0;

            for (Citizen citizen : world.getCitizens()) {
                if (!citizen.isAlive()) {
                    continue;
                }
                int id = citizen.getId();
                if (citizen.isPregnant()) {
                    int start = citizen.getPregnancyStartDay();
                    require(start >= 1 && start <= day,
                            "seed " + seed + " bad pregnancy start at " + day);
                    if (citizen.getLastBirthDay() >= 0) {
                        require(start - citizen.getLastBirthDay()
                                        >= GameConfig.MIN_BIRTH_SPACING_DAYS,
                                "seed " + seed + " conception ignored one-year rest at " + day);
                    }
                    int gestation = citizen.getGestationDay(day);
                    require(gestation >= 0 && gestation <= GameConfig.GESTATION_DAYS
                                    && gestation == Math.min(GameConfig.GESTATION_DAYS, day - start),
                            "seed " + seed + " gestation out of range at " + day);
                }

                Snapshot before = previous.get(id);
                if (before == null) {
                    // Newborn: born on this exact day, never pregnant itself.
                    require(citizen.getLastBirthDay() == -1 && !citizen.isPregnant(),
                            "seed " + seed + " newborn has birth history at " + day);
                    continue;
                }

                if (before.lastBirthDay != citizen.getLastBirthDay()) {
                    require(citizen.getLastBirthDay() == day,
                            "seed " + seed + " delivery recorded on wrong day");
                    require(before.pregnant
                                    && before.pregnancyStartDay + GameConfig.GESTATION_DAYS == day,
                            "seed " + seed + " delivery before the 270th day");
                    if (before.lastBirthDay >= 0) {
                        require(before.pregnancyStartDay - before.lastBirthDay
                                        >= GameConfig.MIN_BIRTH_SPACING_DAYS,
                                "seed " + seed + " second birth ignored one-year rest");
                    }
                    deliveredToday++;
                }

                if (before.pregnant) {
                    if (day < before.pregnancyStartDay + GameConfig.GESTATION_DAYS) {
                        require(citizen.isPregnant()
                                        && citizen.getPregnancyStartDay() == before.pregnancyStartDay,
                                "seed " + seed + " pregnancy interrupted early at " + day);
                    } else {
                        require(!citizen.isPregnant() && citizen.getLastBirthDay() == day,
                                "seed " + seed + " no delivery on the exact due day " + day);
                    }
                }

                if (citizen.isPregnant() && !before.pregnant) {
                    require(citizen.getPregnancyStartDay() == day,
                            "seed " + seed + " conception start day mismatch at " + day);
                    require(before.lastBirthDay < 0
                                    || day - before.lastBirthDay
                                    >= GameConfig.MIN_BIRTH_SPACING_DAYS,
                            "seed " + seed + " conception too soon after birth at " + day);
                }
            }

            require(births - previousBirths == deliveredToday,
                    "seed " + seed + " births delta " + (births - previousBirths)
                            + " != deliveries " + deliveredToday + " at " + day);
            deliveries += deliveredToday;
            previousBirths = births;
        }

        int finalDay = world.getDay();
        require(finalDay == 1 + days, "seed " + seed + " final day " + finalDay);
        return new SeedResult(finalDay, previousBirths, deliveries);
    }

    private static final class Snapshot {
        final boolean pregnant;
        final int pregnancyStartDay;
        final int lastBirthDay;

        Snapshot(Citizen citizen) {
            this.pregnant = citizen.isPregnant();
            this.pregnancyStartDay = citizen.getPregnancyStartDay();
            this.lastBirthDay = citizen.getLastBirthDay();
        }
    }

    private static final class SeedResult {
        final int finalDay;
        final int births;
        final int deliveries;

        SeedResult(int finalDay, int births, int deliveries) {
            this.finalDay = finalDay;
            this.births = births;
            this.deliveries = deliveries;
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

    private static void eq(Object actual, Object expected, String label) {
        require(expected.equals(actual), label + ": expected " + expected + " but was " + actual);
    }

    private static String describe(Throwable failure) {
        String message = failure.getMessage();
        return failure.getClass().getSimpleName()
                + (message == null || message.isEmpty() ? "" : ": " + message);
    }
}
