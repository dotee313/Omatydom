package com.dotee313.omatydom.core;

import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Idea;
import com.dotee313.omatydom.core.model.Ideology;
import com.dotee313.omatydom.core.model.Job;
import com.dotee313.omatydom.core.model.Race;
import com.dotee313.omatydom.core.model.Religion;
import com.dotee313.omatydom.core.model.Ruler;
import com.dotee313.omatydom.core.model.SocialClass;
import com.dotee313.omatydom.core.world.Agent;
import com.dotee313.omatydom.core.world.Animal;
import com.dotee313.omatydom.core.world.AnimalType;
import com.dotee313.omatydom.core.world.IslandMap;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.TileType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Mutable state of the island. The engine changes it; front-ends only read it. */
public final class World {
    private final String name;
    private final IslandMap map;
    private final List<Citizen> citizens = new ArrayList<>();
    private final List<Agent> agents = new ArrayList<>();
    private final List<Animal> animals = new ArrayList<>();
    private final List<GameEvent> history = new ArrayList<>();

    private int day = 1;
    private int nextCitizenId = 1;
    private Ruler ruler;
    private double gold;
    private double food;
    private double wood;
    private double stone;
    private double ore;
    private double crystal;
    private double stability;

    public World(String name, IslandMap map) {
        this.name = name == null ? "Omatydom" : name;
        this.map = map == null ? new IslandMap(GameConfig.MAP_WIDTH, GameConfig.MAP_HEIGHT) : map;
    }

    /** Build a fresh island with terrain, wildlife, resources and a first ruler. */
    public static World create(String name, int population, Random random) {
        IslandMap map = IslandMap.generate(GameConfig.MAP_WIDTH, GameConfig.MAP_HEIGHT, random);
        World world = new World(name, map);

        for (int i = 0; i < 3; i++) {
            int[] spot = map.randomTileOf(TileType.GRASS, random);
            if (spot != null) {
                map.placeStructure(spot[0], spot[1], Structure.HUT);
            }
        }

        int count = Math.max(1, population);
        for (int i = 0; i < count; i++) {
            Race race = pick(Race.values(), random);
            Job job = random.nextDouble() < 0.65 ? race.getPreferredJob() : pick(Job.values(), random);
            if (job == Job.RULER) {
                job = Job.LABORER;
            }
            Gender gender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
            int ageYears = 18 + random.nextInt(50);

            Citizen citizen = new Citizen(
                    world.nextId(),
                    NameGenerator.fullName(random),
                    gender,
                    race,
                    pick(Religion.values(), random),
                    pick(Ideology.values(), random),
                    ageYears * GameConfig.DAYS_PER_YEAR
            );
            citizen.setJob(job);
            citizen.setOccupation(job.getDisplayName());
            citizen.setIdea(pick(Idea.values(), random));
            citizen.setHealth(60 + random.nextInt(40));
            citizen.setHappiness(35 + random.nextInt(50));
            citizen.setWealth(random.nextInt(80));
            citizen.setEducation(random.nextInt(70));
            citizen.setLoyalty(40 + random.nextInt(50));
            citizen.setSocialClass(classFor(citizen, random));
            world.citizens.add(citizen);

            int[] position = map.randomWalkable(random);
            world.agents.add(new Agent(citizen, position[0], position[1], job));
        }

        for (int i = 0; i < GameConfig.INITIAL_ANIMALS; i++) {
            spawnAnimal(world, random);
        }

        world.gold = 200;
        world.food = 180;
        world.wood = 45;
        world.stone = 25;
        world.ore = 6;
        world.crystal = 0;
        world.stability = 60;

        List<Citizen> adults = world.getLivingAdults();
        if (!adults.isEmpty()) {
            Citizen leader = adults.get(random.nextInt(adults.size()));
            world.ruler = new Ruler(leader, GameConfig.RULER_TITLE, 1);
            leader.setJob(Job.RULER);
            leader.setOccupation(Job.RULER.getDisplayName());
            leader.setSocialClass(SocialClass.NOBLE);
            Agent leaderAgent = world.findAgent(leader);
            if (leaderAgent != null) {
                leaderAgent.setJob(Job.RULER);
            }
        }

        world.addEvent(new GameEvent(1, EventType.POLITICS, "The island awakens",
                "A new chronicle begins on the shores of " + world.name + ".", 2));
        return world;
    }

    private static SocialClass classFor(Citizen citizen, Random random) {
        if (!citizen.isAdult()) {
            return SocialClass.CHILD;
        }
        switch (citizen.getJob()) {
            case RULER:
                return SocialClass.NOBLE;
            case PRIEST:
                return SocialClass.CLERGY;
            case BUILDER:
            case MINER:
            case TRADER:
            case WOODCUTTER:
                return random.nextDouble() < 0.7 ? SocialClass.ARTISAN : SocialClass.LABORER;
            default:
                if (citizen.getWealth() > 70 && random.nextDouble() < 0.35) {
                    return SocialClass.NOBLE;
                }
                return SocialClass.LABORER;
        }
    }

    /** Add one animal in a suitable habitat. */
    public static void spawnAnimal(World world, Random random) {
        double roll = random.nextDouble();
        AnimalType type = roll < 0.35 ? AnimalType.FISH
                : (roll < 0.68 ? AnimalType.DEER : AnimalType.BOAR);
        int[] spot;
        if (type.isWater()) {
            spot = world.map.randomTileOf(TileType.WATER, random);
        } else {
            spot = world.map.randomTileOf(random.nextBoolean() ? TileType.FOREST : TileType.GRASS, random);
        }
        if (spot == null) {
            spot = world.map.randomWalkable(random);
        }
        world.animals.add(new Animal(type, spot[0], spot[1]));
    }

    private static <T> T pick(T[] values, Random random) {
        return values[random.nextInt(values.length)];
    }

    public String getName() {
        return name;
    }

    public IslandMap getMap() {
        return map;
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

    public List<Agent> getAgents() {
        return Collections.unmodifiableList(agents);
    }

    public void addAgent(Agent agent) {
        if (agent != null) {
            agents.add(agent);
        }
    }

    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    public Agent findAgent(Citizen citizen) {
        for (Agent agent : agents) {
            if (agent.getCitizen() == citizen) {
                return agent;
            }
        }
        return null;
    }

    public List<Animal> getAnimals() {
        return Collections.unmodifiableList(animals);
    }

    public void addAnimal(Animal animal) {
        if (animal != null) {
            animals.add(animal);
        }
    }

    public void removeAnimal(Animal animal) {
        animals.remove(animal);
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

    public double getWood() {
        return wood;
    }

    public void setWood(double wood) {
        this.wood = Math.max(0, wood);
    }

    public double getStone() {
        return stone;
    }

    public void setStone(double stone) {
        this.stone = Math.max(0, stone);
    }

    public double getOre() {
        return ore;
    }

    public void setOre(double ore) {
        this.ore = Math.max(0, ore);
    }

    public double getCrystal() {
        return crystal;
    }

    public void setCrystal(double crystal) {
        this.crystal = Math.max(0, crystal);
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
