package com.dotee313.omatydom.core;

import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Religion;
import com.dotee313.omatydom.core.model.Ruler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * The public face of the game. Both the desktop runner and the Android app talk
 * to this class instead of touching the simulation internals.
 */
public final class OmatydomGame {
    private static final int[] SPEEDS = {1, 2, 3, 5, 10};

    private final World world;
    private final SimulationEngine engine;
    private final Random random;

    private boolean paused;
    private int speed = 1;
    private boolean playerControlsRuler;

    private OmatydomGame(World world, Random random) {
        this.world = world;
        this.random = random;
        this.engine = new SimulationEngine(world, random);
    }

    /** Create a reproducible island from a seed. */
    public static OmatydomGame create(long seed) {
        Random random = new Random(seed);
        World world = World.create("Omatydom", GameConfig.INITIAL_POPULATION, random);
        return new OmatydomGame(world, random);
    }

    /** Create an island with a random seed. */
    public static OmatydomGame create() {
        return create(System.currentTimeMillis());
    }

    public World getWorld() {
        return world;
    }

    public SimulationEngine getEngine() {
        return engine;
    }

    // ------------------------------------------------------------------
    // Time control
    // ------------------------------------------------------------------

    /** Advance the clock by one tick (which may be several days). */
    public void tick() {
        if (paused) {
            return;
        }
        for (int i = 0; i < speed; i++) {
            engine.advanceDay();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void togglePause() {
        this.paused = !this.paused;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = Math.max(1, Math.min(10, speed));
    }

    public void cycleSpeed() {
        int index = 0;
        for (int i = 0; i < SPEEDS.length; i++) {
            if (SPEEDS[i] == speed) {
                index = (i + 1) % SPEEDS.length;
                break;
            }
        }
        speed = SPEEDS[index];
    }

    // ------------------------------------------------------------------
    // Player agency
    // ------------------------------------------------------------------

    public boolean isPlayerControlled() {
        return playerControlsRuler;
    }

    /** Take direct control of the ruler's decisions. */
    public void takeControl() {
        if (playerControlsRuler) {
            return;
        }
        playerControlsRuler = true;
        engine.postEvent(new GameEvent(world.getDay(), EventType.POLITICS, "You take the throne",
                "From this day forward, the ruler's choices are yours.", 4));
    }

    /** Let the ruler act on their own again and return to pure observation. */
    public void releaseControl() {
        if (!playerControlsRuler) {
            return;
        }
        playerControlsRuler = false;
        engine.postEvent(new GameEvent(world.getDay(), EventType.POLITICS, "You step back",
                "The ruler is left to their own judgement once more.", 3));
    }

    public void togglePlayerControl() {
        if (playerControlsRuler) {
            releaseControl();
        } else {
            takeControl();
        }
    }

    /** Living adults, ordered by how well placed they are to rule. */
    public List<Citizen> getRulerCandidates() {
        List<Citizen> candidates = world.getLivingAdults();
        candidates.sort(Comparator.comparingDouble(
                (Citizen c) -> c.getLoyalty() + c.getWealth() + c.getEducation() * 0.3).reversed());
        return candidates;
    }

    /** Appoint a specific citizen as ruler. */
    public boolean chooseRuler(int citizenId) {
        Citizen chosen = findCitizen(citizenId);
        if (chosen == null || !chosen.isAlive() || !chosen.isAdult()) {
            return false;
        }
        Ruler old = world.getRuler();
        String oldName = old == null ? "no one" : old.getCitizen().getName();
        world.setRuler(new Ruler(chosen, GameConfig.RULER_TITLE, world.getDay()));
        engine.postEvent(new GameEvent(world.getDay(), EventType.SUCCESSION, "A ruler is chosen",
                oldName + " gives way to " + chosen.getName() + ".", 4));
        return true;
    }

    /** Remove the ruler and immediately trigger a succession. */
    public boolean assassinateRuler() {
        Ruler ruler = world.getRuler();
        if (ruler == null) {
            return false;
        }
        Citizen victim = ruler.getCitizen();
        victim.setAlive(false);
        victim.setHealth(0);
        world.setRuler(null);
        world.setStability(world.getStability() - 20);
        engine.postEvent(new GameEvent(world.getDay(), EventType.ASSASSINATION, "The ruler falls",
                victim.getName() + " has been assassinated. The island holds its breath.", 5));
        engine.holdSuccession(world.getDay(), "A successor must be found");
        return true;
    }

    /** Apply a decree to the island. Returns false only for a null decree. */
    public boolean issueDecree(Decree decree) {
        if (decree == null) {
            return false;
        }

        Ruler ruler = world.getRuler();
        String rulerName = ruler == null ? "the council" : ruler.getCitizen().getName();

        world.setGold(world.getGold() + decree.getGoldEffect());
        world.setStability(world.getStability() + decree.getStabilityEffect());

        for (Citizen citizen : world.getLivingCitizens()) {
            citizen.setHappiness(citizen.getHappiness()
                    + decree.getHappinessEffect() + jitter());
            citizen.setLoyalty(citizen.getLoyalty()
                    + decree.getLoyaltyEffect() + jitter());
            citizen.setWealth(citizen.getWealth() + decree.getWealthEffect());
            if (decree == Decree.BUILD_TEMPLE && random.nextDouble() < 0.05) {
                Religion[] faiths = Religion.values();
                citizen.setReligion(faiths[random.nextInt(faiths.length)]);
            }
        }

        if (ruler != null) {
            ruler.setDecreesIssued(ruler.getDecreesIssued() + 1);
        }
        engine.postEvent(new GameEvent(world.getDay(), EventType.DECREE,
                decree.getDisplayName(),
                decree.getDescription() + " (decreed by " + rulerName + ")", 3));
        return true;
    }

    private Citizen findCitizen(int id) {
        for (Citizen citizen : world.getCitizens()) {
            if (citizen.getId() == id) {
                return citizen;
            }
        }
        return null;
    }

    private double jitter() {
        return random.nextDouble() * 2 - 1;
    }
}
