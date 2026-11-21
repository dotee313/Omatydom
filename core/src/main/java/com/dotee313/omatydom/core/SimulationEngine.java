package com.dotee313.omatydom.core;

import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Ideology;
import com.dotee313.omatydom.core.model.Race;
import com.dotee313.omatydom.core.model.Religion;
import com.dotee313.omatydom.core.model.Ruler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Advances the island by one day at a time. All rules live here; the facade and
 * the user interfaces only decide when to call it.
 */
public final class SimulationEngine {
    private final World world;
    private final Random random;
    private final List<SimulationListener> listeners = new ArrayList<>();

    public SimulationEngine(World world, Random random) {
        this.world = world;
        this.random = random;
    }

    public World getWorld() {
        return world;
    }

    public void addListener(SimulationListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }

    /** Publish an event to the chronicle and to every attached front-end. */
    public void postEvent(GameEvent event) {
        if (event == null) {
            return;
        }
        world.addEvent(event);
        for (SimulationListener listener : new ArrayList<>(listeners)) {
            listener.onEvent(event);
        }
    }

    /** Advance the island by exactly one day. */
    public void advanceDay() {
        int day = world.getDay() + 1;
        world.setDay(day);

        double population = Math.max(1, world.getPopulation());

        stepEconomy(population);
        double mood = computeMood();
        stepCitizens(day, population, mood);
        stepBirths(day, mood);
        stepPolitics(day, mood);

        world.setStability(clamp(world.getStability() + (mood - 50) / 50.0 * 0.8
                + random.nextDouble() * 0.4 - 0.2));

        maybeGenerateEvent(day);

        for (SimulationListener listener : new ArrayList<>(listeners)) {
            listener.onDayAdvanced(day);
        }
    }

    private void stepEconomy(double population) {
        double production = GameConfig.BASE_FOOD_PRODUCTION
                + population * GameConfig.FOOD_PER_WORKER;
        double consumption = population * GameConfig.FOOD_PER_CITIZEN;
        double spoilage = world.getFood() * GameConfig.FOOD_SPOILAGE_RATE;
        world.setFood(world.getFood() + production - consumption - spoilage);

        double income = population * GameConfig.TAX_PER_CITIZEN
                * (0.5 + world.getStability() / 200.0);
        double upkeep = population * GameConfig.ADMIN_COST_PER_CITIZEN;
        world.setGold(world.getGold() + income - upkeep);
    }

    private void stepCitizens(int day, double population, double mood) {
        List<Citizen> snapshot = new ArrayList<>(world.getCitizens());
        for (Citizen citizen : snapshot) {
            if (!citizen.isAlive()) {
                continue;
            }

            citizen.setAgeDays(citizen.getAgeDays() + 1);

            double foodPerCitizen = world.getFood() / population;
            double happinessDelta = (mood - 50) / 50.0 * 0.6
                    + random.nextDouble() * 0.3 - 0.15;
            double healthDelta = (foodPerCitizen >= 1 ? 0.04 : -0.3)
                    + random.nextDouble() * 0.12 - 0.06;
            double wealthDelta = (world.getGold() / population) * 0.002
                    + random.nextDouble() * 0.2 - 0.1;
            double loyaltyDelta = (mood - 50) / 50.0 * 0.25
                    + (world.getRuler() == null ? -1.5 : 0)
                    + random.nextDouble() * 0.3 - 0.15;

            citizen.setHappiness(citizen.getHappiness() + happinessDelta);
            citizen.setHealth(citizen.getHealth() + healthDelta);
            citizen.setWealth(citizen.getWealth() + wealthDelta);
            citizen.setLoyalty(citizen.getLoyalty() + loyaltyDelta);
            citizen.setEducation(citizen.getEducation() + random.nextDouble() * 0.05);

            if (random.nextDouble() < 0.002) {
                citizen.setReligion(pick(Religion.values()));
            }
            if (random.nextDouble() < 0.0015) {
                citizen.setIdeology(pick(Ideology.values()));
            }

            double ageYears = citizen.getAgeYears();
            double deathChance = 0.00015
                    + Math.max(0, ageYears - 60) * 0.00006
                    + (citizen.getHealth() < 25 ? 0.008 : 0);
            if (random.nextDouble() < deathChance) {
                citizen.setAlive(false);
                citizen.setHealth(0);
                postEvent(new GameEvent(day, EventType.DEATH, "A life ends",
                        citizen.getName() + " has died at the age of " + ageYears + ".", 2));
            }
        }
    }

    private void stepBirths(int day, double mood) {
        double expected = world.getPopulation()
                * GameConfig.BIRTH_RATE_PER_CITIZEN
                * (0.3 + mood / 100.0);

        while (expected >= 1.0 || random.nextDouble() < expected) {
            if (expected >= 1.0) {
                expected -= 1.0;
            } else {
                expected = 0;
            }
            if (world.getPopulation() >= GameConfig.MAX_POPULATION) {
                break;
            }
            List<Citizen> adults = world.getLivingAdults();
            if (adults.isEmpty()) {
                break;
            }
            Citizen parent = pick(adults);
            Citizen child = new Citizen(
                    world.nextId(),
                    NameGenerator.fullName(random),
                    random.nextBoolean() ? Gender.MALE : Gender.FEMALE,
                    parent.getRace(),
                    parent.getReligion(),
                    pick(Ideology.values()),
                    0
            );
            child.setHappiness(60);
            child.setHealth(70);
            child.setLoyalty(parent.getLoyalty());
            world.addCitizen(child);
            postEvent(new GameEvent(day, EventType.BIRTH, "New life",
                    "A child named " + child.getName() + " was born to " + parent.getName() + ".", 1));
        }
    }

    private void stepPolitics(int day, double mood) {
        Ruler ruler = world.getRuler();
        if (ruler == null || !ruler.getCitizen().isAlive()) {
            holdSuccession(day, "The throne is empty");
            return;
        }

        ruler.setApproval(clamp(ruler.getApproval()
                + (mood - 50) / 50.0 * 1.2
                - 0.15
                + random.nextDouble() * 0.4 - 0.2));

        boolean unstable = world.getStability() < 35 && ruler.getApproval() < 25;
        if (unstable && random.nextDouble() < 0.02) {
            postEvent(new GameEvent(day, EventType.COUP, "The throne is stormed",
                    "Soldiers surround the palace of " + ruler.getCitizen().getName()
                            + ". A coup is under way.", 5));
            world.setStability(world.getStability() - 15);
            holdSuccession(day, "A coup has overthrown " + ruler.getCitizen().getName());
        }
    }

    /** Choose a new leader from the living adults. Public so interventions can trigger it. */
    public void holdSuccession(int day, String reason) {
        List<Citizen> adults = world.getLivingAdults();
        if (adults.isEmpty()) {
            world.setRuler(null);
            return;
        }
        Citizen best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Citizen citizen : adults) {
            double score = citizen.getLoyalty() + citizen.getWealth()
                    + citizen.getEducation() * 0.3 + random.nextDouble() * 25;
            if (score > bestScore) {
                bestScore = score;
                best = citizen;
            }
        }
        if (best == null) {
            return;
        }
        Ruler ruler = new Ruler(best, GameConfig.RULER_TITLE, day);
        world.setRuler(ruler);
        postEvent(new GameEvent(day, EventType.SUCCESSION, "A new ruler rises",
                (reason == null || reason.isEmpty() ? "The island needs a leader" : reason)
                        + ". " + best.getName() + " now holds the throne.", 4));
    }

    private double computeMood() {
        List<Citizen> living = world.getLivingCitizens();
        if (living.isEmpty()) {
            return 0;
        }
        double happinessSum = 0;
        for (Citizen citizen : living) {
            happinessSum += citizen.getHappiness();
        }
        double averageHappiness = happinessSum / living.size();
        double foodPerCitizen = world.getFood() / living.size();

        double mood = 50;
        mood += Math.max(-20, Math.min(20, foodPerCitizen * 2 - 10));
        mood += (world.getStability() - 50) * 0.3;
        return clamp(mood * 0.6 + averageHappiness * 0.4);
    }

    private void maybeGenerateEvent(int day) {
        if (random.nextDouble() > 0.09) {
            return;
        }
        switch (random.nextInt(9)) {
            case 0: {
                Religion faith = pick(Religion.values());
                int converts = 0;
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (random.nextDouble() < 0.08) {
                        citizen.setReligion(faith);
                        converts++;
                    }
                }
                postEvent(new GameEvent(day, EventType.CONVERSION, "A wave of faith",
                        converts + " islanders have turned to " + faith.getDisplayName() + ".", 3));
                break;
            }
            case 1: {
                world.setFood(world.getFood() - 35);
                postEvent(new GameEvent(day, EventType.DISASTER, "Drought",
                        "The rains fail. The storehouses are " + (int) world.getFood() + " food short.", 4));
                break;
            }
            case 2: {
                world.setGold(world.getGold() - 8);
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setHappiness(citizen.getHappiness() + 7);
                }
                postEvent(new GameEvent(day, EventType.FESTIVAL, "Lantern night",
                        "The village square glows with lanterns. Spirits are high.", 2));
                break;
            }
            case 3: {
                world.setGold(world.getGold() + 30);
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setWealth(citizen.getWealth() + 3);
                }
                postEvent(new GameEvent(day, EventType.ECONOMY, "A merchant ship arrives",
                        "A foreign trader docks with gold and strange goods.", 2));
                break;
            }
            case 4: {
                world.setStability(world.getStability() - 10);
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setHappiness(citizen.getHappiness() - 5);
                }
                postEvent(new GameEvent(day, EventType.UNREST, "Rumours in the market",
                        "Angry voices gather in the market square. Stability falls.", 3));
                break;
            }
            case 5: {
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setEducation(citizen.getEducation() + 6);
                }
                postEvent(new GameEvent(day, EventType.DISCOVERY, "A scholar's discovery",
                        "An old map is found in the ruins. The island learns its own past.", 2));
                break;
            }
            case 6: {
                Ideology idea = pick(Ideology.values());
                int converts = 0;
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (random.nextDouble() < 0.07) {
                        citizen.setIdeology(idea);
                        converts++;
                    }
                }
                postEvent(new GameEvent(day, EventType.IDEOLOGY, "A new argument spreads",
                        converts + " islanders now call themselves " + idea.getDisplayName() + "s.", 3));
                break;
            }
            case 7: {
                int arrivals = 1 + random.nextInt(3);
                for (int i = 0; i < arrivals && world.getPopulation() < GameConfig.MAX_POPULATION; i++) {
                    Citizen newcomer = new Citizen(
                            world.nextId(),
                            NameGenerator.fullName(random),
                            random.nextBoolean() ? Gender.MALE : Gender.FEMALE,
                            pick(Race.values()),
                            pick(Religion.values()),
                            pick(Ideology.values()),
                            (18 + random.nextInt(25)) * 365
                    );
                    newcomer.setWealth(random.nextInt(40));
                    world.addCitizen(newcomer);
                }
                postEvent(new GameEvent(day, EventType.ECONOMY, "A raft from abroad",
                        arrivals + " newcomers wash onto the beach and ask to stay.", 3));
                break;
            }
            case 8:
            default: {
                int sick = 0;
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (random.nextDouble() < 0.12) {
                        citizen.setHealth(citizen.getHealth() - 20);
                        sick++;
                    }
                }
                postEvent(new GameEvent(day, EventType.DISASTER, "Fever on the shore",
                        sick + " islanders fall ill after the tide turns.", 3));
                break;
            }
        }
    }

    private <T> T pick(T[] values) {
        return values[random.nextInt(values.length)];
    }

    private <T> T pick(List<T> values) {
        return values.get(random.nextInt(values.size()));
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }
}
