package com.dotee313.omatydom.core;

import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Ideology;
import com.dotee313.omatydom.core.model.Race;
import com.dotee313.omatydom.core.model.Religion;
import com.dotee313.omatydom.core.model.Ruler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Mutable state of the island. The engine changes it; front-ends only read it. */
public final class World {
    private final String name;
    private final List<Citizen> citizens = new ArrayList<>();
    private final List<GameEvent> history = new ArrayList<>();

    private int day = 1;
    private int nextCitizenId = 1;
    private Ruler ruler;
    private double gold;
    private double food;
    private double stability;

    public World(String name) {
        this.name = name == null ? "Omatydom" : name;
    }

    /** Build a fresh island with a random population and a first ruler. */
    public static World create(String name, int population, Random random) {
        World world = new World(name);
        int count = Math.max(1, population);

        for (int i = 0; i < count; i++) {
            Race race = pick(Race.values(), random);
            Religion religion = pick(Religion.values(), random);
            Ideology ideology = pick(Ideology.values(), random);
            Gender gender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
            int ageYears = 18 + random.nextInt(50);

            Citizen citizen = new Citizen(
                    world.nextId(),
                    NameGenerator.fullName(random),
                    gender,
                    race,
                    religion,
                    ideology,
                    ageYears * 365
            );
            citizen.setHealth(60 + random.nextInt(40));
            citizen.setHappiness(35 + random.nextInt(50));
            citizen.setWealth(random.nextInt(80));
            citizen.setEducation(random.nextInt(70));
            citizen.setLoyalty(40 + random.nextInt(50));
            citizen.setOccupation(occupationFor(race, random));
            world.citizens.add(citizen);
        }

        world.gold = 200;
        world.food = 300;
        world.stability = 60;

        List<Citizen> adults = world.getLivingAdults();
        if (!adults.isEmpty()) {
            world.ruler = new Ruler(adults.get(random.nextInt(adults.size())),
                    GameConfig.RULER_TITLE, 1);
        }
        world.addEvent(new GameEvent(1, EventType.POLITICS, "The island awakens",
                "A new chronicle begins on the shores of " + world.name + ".", 2));
        return world;
    }

    private static String occupationFor(Race race, Random random) {
        switch (race) {
            case DWARF:
                return random.nextBoolean() ? "Mason" : "Smith";
            case ELF:
                return random.nextBoolean() ? "Herbalist" : "Scholar";
            case ORC:
                return random.nextBoolean() ? "Guard" : "Fisher";
            case HALFLING:
                return random.nextBoolean() ? "Merchant" : "Cook";
            default:
                return random.nextBoolean() ? "Farmer" : "Fisher";
        }
    }

    private static <T> T pick(T[] values, Random random) {
        return values[random.nextInt(values.length)];
    }

    public String getName() {
        return name;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = Math.max(1, day);
    }

    public int nextId() {
        return nextCitizenId++;
    }

    public List<Citizen> getCitizens() {
        return Collections.unmodifiableList(citizens);
    }

    public void addCitizen(Citizen citizen) {
        if (citizen != null) {
            citizens.add(citizen);
        }
    }

    public List<Citizen> getLivingCitizens() {
        List<Citizen> living = new ArrayList<>();
        for (Citizen citizen : citizens) {
            if (citizen.isAlive()) {
                living.add(citizen);
            }
        }
        return living;
    }

    public List<Citizen> getLivingAdults() {
        List<Citizen> adults = new ArrayList<>();
        for (Citizen citizen : citizens) {
            if (citizen.isAlive() && citizen.isAdult()) {
                adults.add(citizen);
            }
        }
        return adults;
    }

    public int getPopulation() {
        return getLivingCitizens().size();
    }

    public Ruler getRuler() {
        return ruler;
    }

    public void setRuler(Ruler ruler) {
        this.ruler = ruler;
    }

    public double getGold() {
        return gold;
    }

    public void setGold(double gold) {
        this.gold = Math.max(0, gold);
    }

    public double getFood() {
        return food;
    }

    public void setFood(double food) {
        this.food = Math.max(0, food);
    }

    public double getStability() {
        return stability;
    }

    public void setStability(double stability) {
        this.stability = Math.max(0, Math.min(100, stability));
    }

    public void addEvent(GameEvent event) {
        if (event == null) {
            return;
        }
        history.add(event);
        if (history.size() > 300) {
            history.remove(0);
        }
    }

    /** Returns the newest events first. */
    public List<GameEvent> getRecentEvents(int count) {
        int size = history.size();
        int from = Math.max(0, size - Math.max(0, count));
        List<GameEvent> result = new ArrayList<>();
        for (int i = size - 1; i >= from; i--) {
            result.add(history.get(i));
        }
        return result;
    }
}
