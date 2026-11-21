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
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;
import com.dotee313.omatydom.core.world.Weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Advances the island by one day at a time. Citizens physically walk the map,
 * harvest resources, raise buildings, hunt animals and trade ideas.
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

        stepWeather(day);
        damageStructuresFromWeather(day);
        if (day > 1 && (day - 1) % GameConfig.DAYS_PER_AGE == 0) {
            postEvent(new GameEvent(day, EventType.AGE, "A new age begins",
                    "The island enters the " + Age.of(day).getDisplayName() + ".", 5));
        }

        double population = Math.max(1, world.getPopulation());
        stepEconomy(population, day);
        stepAgents(day);
        stepAnimals();
        stepMonsters(day);
        stepIdeas();

        double mood = computeMood();
        stepCitizens(day, population, mood);
        stepBirths(day, mood);
        stepPolitics(day, mood);

        // Stability eases towards a target instead of drifting to a permanent zero.
        double stabilityTarget = clamp(50 + (mood - 50) * 0.7);
        double stabilityNow = world.getStability();
        world.setStability(stabilityNow + (stabilityTarget - stabilityNow) * 0.05
                + random.nextDouble() * 0.6 - 0.3);
        world.getMap().regenerate(random);
        maybeGenerateEvent(day);

        for (SimulationListener listener : new ArrayList<>(listeners)) {
            listener.onDayAdvanced(day);
        }
    }

    /** Weather persists for a few days so it feels like a season, not noise. */
    private void stepWeather(int day) {
        if (world.getWeatherDaysLeft() > 1 && random.nextDouble() < 0.75) {
            world.setWeatherDaysLeft(world.getWeatherDaysLeft() - 1);
            return;
        }
        int season = OmatydomGame.seasonOf(day);
        double roll = random.nextDouble();
        Weather next;
        if (season == 3) {
            next = roll < 0.45 ? Weather.SNOW
                    : (roll < 0.70 ? Weather.STORM : Weather.CLEAR);
        } else if (season == 1) {
            next = roll < 0.30 ? Weather.DROUGHT
                    : (roll < 0.55 ? Weather.STORM : Weather.CLEAR);
        } else {
            next = roll < 0.42 ? Weather.CLEAR
                    : (roll < 0.72 ? Weather.RAIN
                    : (roll < 0.85 ? Weather.STORM : Weather.DROUGHT));
        }
        if (random.nextDouble() < 0.04) {
            next = Weather.ASH;
        }
        world.setWeather(next);
        world.setWeatherDaysLeft(2 + random.nextInt(5));
        if (postWeatherEvent(next)) {
            postEvent(new GameEvent(day, EventType.WEATHER, "The sky changes",
                    next.getDisplayName() + " settles over the island.", 1));
        }
    }

    /** Dangerous creatures hurt nearby islanders each day. */
    private void stepMonsters(int day) {
        for (Animal animal : new ArrayList<>(world.getAnimals())) {
            if (!animal.isAlive() || !animal.getType().isDangerous()) {
                continue;
            }
            for (Agent agent : new ArrayList<>(world.getAgents())) {
                if (!agent.getCitizen().isAlive()
                        || distance(animal.getX(), animal.getY(), agent.getX(), agent.getY()) > 1
                        || random.nextDouble() >= 0.25) {
                    continue;
                }
                Citizen victim = agent.getCitizen();
                double damage = animal.getType() == AnimalType.DRAGON ? 10
                        : (animal.getType() == AnimalType.GOLEM ? 6 : 4);
                victim.setHealth(victim.getHealth() - damage);
                if (victim.getHealth() <= 0) {
                    killCitizenByCause(victim, day, animal.getType().getDisplayName());
                } else if (random.nextDouble() < 0.02) {
                    postEvent(new GameEvent(day, EventType.DISASTER, "A creature attacks",
                            animal.getType().getDisplayName() + " wounded "
                                    + victim.getName() + ".", 2));
                }
            }
        }
    }

    private void killCitizenByCause(Citizen citizen, int day, String cause) {
        citizen.setAlive(false);
        citizen.setHealth(0);
        world.incrementDeaths();
        Agent agent = world.findAgent(citizen);
        if (agent != null) {
            world.removeAgent(agent);
        }
        if (world.getRuler() != null
                && citizen.getId() == world.getRuler().getCitizen().getId()) {
            world.setRuler(null);
        }
        postEvent(new GameEvent(day, EventType.DEATH, "A life cut short",
                citizen.getName() + " was killed by " + cause + ".", 3));
    }

    private void damageStructuresFromWeather(int day) {
        double chance = world.getWeather().getStructureDamageChance();
        if (chance <= 0) {
            return;
        }
        for (int y = 0; y < world.getMap().getHeight(); y++) {
            for (int x = 0; x < world.getMap().getWidth(); x++) {
                Tile tile = world.getMap().get(x, y);
                if (tile != null && tile.getStructure() != Structure.NONE
                        && random.nextDouble() < chance) {
                    tile.setStructure(Structure.NONE);
                    postEvent(new GameEvent(day, EventType.WEATHER, "Weather damage",
                            "A storm tore down a structure at (" + x + "," + y + ").", 2));
                }
            }
        }
    }

    private static boolean postWeatherEvent(Weather weather) {
        return weather != Weather.CLEAR;
    }

    private void stepEconomy(double population, int day) {
        regrowResources();
        double before = world.getFood();
        world.setFood(world.getFood() - population * GameConfig.FOOD_PER_CITIZEN);

        if (before < population * GameConfig.FOOD_PER_CITIZEN) {
            for (Citizen citizen : world.getLivingCitizens()) {
                citizen.setHappiness(citizen.getHappiness() - 3);
                citizen.setHealth(citizen.getHealth() - 1.5);
            }
            if (random.nextDouble() < 0.08) {
                postEvent(new GameEvent(day, EventType.DISASTER, "Empty storehouses",
                        "There is not enough food to go around. The island goes hungry.", 4));
            }
        }

        double income = population * GameConfig.TAX_PER_CITIZEN
                * (0.5 + world.getStability() / 200.0);
        double upkeep = population * GameConfig.ADMIN_COST_PER_CITIZEN;
        world.setGold(world.getGold() + income - upkeep);
    }

    /** Gentle daily regrowth; rain and green ages make the island recover faster. */
    private void regrowResources() {
        double chance = 0.010 * world.getWeather().getRegrowthMultiplier()
                * Age.of(world.getDay()).getWorkMultiplier();
        for (int y = 0; y < world.getMap().getHeight(); y++) {
            for (int x = 0; x < world.getMap().getWidth(); x++) {
                Tile tile = world.getMap().get(x, y);
                if (tile == null || tile.getResource() >= 24) {
                    continue;
                }
                TileType type = tile.getType();
                if ((type == TileType.FOREST || type == TileType.ORCHARD
                        || type == TileType.MOUNTAIN || type == TileType.CAVE)
                        && random.nextDouble() < chance / 4) {
                    tile.setResource(Math.min(24, tile.getResource() + 1));
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Agents and work
    // ------------------------------------------------------------------

    private void stepAgents(int day) {
        IslandMap map = world.getMap();
        for (Agent agent : new ArrayList<>(world.getAgents())) {
            Citizen citizen = agent.getCitizen();
            if (!citizen.isAlive()) {
                world.removeAgent(agent);
                continue;
            }
            if (agent.getJob() != citizen.getJob()) {
                agent.setJob(citizen.getJob());
            }
            if (!agent.hasTarget() || !targetIsValid(agent)) {
                assignTarget(agent);
            }
            if (agent.hasTarget()) {
                moveTowards(agent, agent.getTargetX(), agent.getTargetY());
            }
            if (atTarget(agent)) {
                work(agent, day);
            } else if (map.typeAt(agent.getX(), agent.getY()) == TileType.WATER) {
                // Safety net: never leave an agent in the sea.
                int[] spot = map.randomWalkable(random);
                agent.setX(spot[0]);
                agent.setY(spot[1]);
                agent.clearTarget();
            }
        }
    }

    private boolean targetIsValid(Agent agent) {
        Job job = agent.getJob();
        IslandMap map = world.getMap();
        if (job.getTarget() != null) {
            Tile tile = map.get(agent.getTargetX(), agent.getTargetY());
            return tile != null && tile.getType() == job.getTarget() && tile.getResource() > 0;
        }
        if (job == Job.HUNTER) {
            Animal animal = agent.getTargetAnimal();
            return animal != null && animal.isAlive();
        }
        if (job == Job.BUILDER) {
            Tile tile = map.get(agent.getTargetX(), agent.getTargetY());
            return tile != null && tile.isFreeLand();
        }
        return true;
    }

    private void assignTarget(Agent agent) {
        Job job = agent.getJob();
        IslandMap map = world.getMap();
        agent.clearTarget();

        switch (job) {
            case WOODCUTTER:
                setTerrainTarget(agent, TileType.FOREST);
                break;
            case GATHERER:
                setTerrainTarget(agent, TileType.ORCHARD);
                break;
            case MINER: {
                int[] spot = findResourceTile(agent.getX(), agent.getY(),
                        TileType.MOUNTAIN, TileType.CAVE);
                if (spot != null) {
                    agent.setTarget(spot[0], spot[1]);
                }
                break;
            }
            case FISHER: {
                int[] shore = map.findNearestShore(agent.getX(), agent.getY());
                if (shore != null) {
                    agent.setTarget(shore[0], shore[1]);
                }
                break;
            }
            case HUNTER: {
                Animal best = null;
                int bestDistance = Integer.MAX_VALUE;
                for (Animal animal : world.getAnimals()) {
                    if (!animal.isAlive() || animal.getType().isWater()) {
                        continue;
                    }
                    int distance = Math.abs(animal.getX() - agent.getX())
                            + Math.abs(animal.getY() - agent.getY());
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = animal;
                    }
                }
                if (best != null) {
                    agent.setTargetAnimal(best);
                    agent.setTarget(best.getX(), best.getY());
                }
                break;
            }
            case BUILDER: {
                int[] site = null;
                int bestDistance = Integer.MAX_VALUE;
                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        Tile tile = map.get(x, y);
                        if (tile == null || !tile.isFreeLand()) {
                            continue;
                        }
                        int distance = Math.abs(x - agent.getX()) + Math.abs(y - agent.getY());
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            site = new int[]{x, y};
                        }
                    }
                }
                if (site != null) {
                    agent.setTarget(site[0], site[1]);
                }
                break;
            }
            case RULER:
            case PRIEST:
            case TRADER:
            case LABORER:
            default:
                if (random.nextDouble() < 0.15) {
                    int[] spot = map.randomWalkable(random);
                    agent.setTarget(spot[0], spot[1]);
                }
                break;
        }
    }

    private void setTerrainTarget(Agent agent, TileType type) {
        int[] spot = findResourceTile(agent.getX(), agent.getY(), type);
        if (spot != null) {
            agent.setTarget(spot[0], spot[1]);
        }
    }

    /** Closest tile of one of the given types that still has resources left. */
    private int[] findResourceTile(int fromX, int fromY, TileType... types) {
        IslandMap map = world.getMap();
        int bestDistance = Integer.MAX_VALUE;
        int[] best = null;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = map.get(x, y);
                if (tile.getResource() <= 0) {
                    continue;
                }
                boolean matches = false;
                for (TileType type : types) {
                    if (tile.getType() == type) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) {
                    continue;
                }
                int distance = Math.abs(x - fromX) + Math.abs(y - fromY);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new int[]{x, y};
                }
            }
        }
        return best;
    }

    private void moveTowards(Agent agent, int targetX, int targetY) {
        IslandMap map = world.getMap();
        int x = agent.getX();
        int y = agent.getY();
        if (x == targetX && y == targetY) {
            return;
        }
        boolean moved = false;
        if (x != targetX && map.isWalkable(x + sign(targetX - x), y)) {
            agent.setX(x + sign(targetX - x));
            moved = true;
        } else if (y != targetY && map.isWalkable(x, y + sign(targetY - y))) {
            agent.setY(y + sign(targetY - y));
            moved = true;
        }
        if (!moved) {
            int[] step = randomWalkableNeighbour(map, x, y);
            if (step != null) {
                agent.setX(step[0]);
                agent.setY(step[1]);
            }
        }
    }

    private int[] randomWalkableNeighbour(IslandMap map, int x, int y) {
        List<int[]> options = new ArrayList<>();
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : dirs) {
            if (map.isWalkable(x + dir[0], y + dir[1])) {
                options.add(new int[]{x + dir[0], y + dir[1]});
            }
        }
        if (options.isEmpty()) {
            return null;
        }
        return options.get(random.nextInt(options.size()));
    }

    private boolean atTarget(Agent agent) {
        return agent.hasTarget()
                && agent.getX() == agent.getTargetX()
                && agent.getY() == agent.getTargetY();
    }

    /** Innate traits make some islanders noticeably better or worse at work. */
    private double workYield(Agent agent, double base) {
        double traitFactor = 1.0 + agent.getCitizen().getTrait().getWorkModifier();
        double weatherFactor = world.getWeather().getWorkMultiplier();
        double ageFactor = Age.of(world.getDay()).getWorkMultiplier();
        return Math.max(base * 0.3, base * traitFactor * weatherFactor * ageFactor);
    }

    private void work(Agent agent, int day) {
        Job job = agent.getJob();
        IslandMap map = world.getMap();
        Tile tile = map.get(agent.getX(), agent.getY());

        switch (job) {
            case WOODCUTTER:
                if (tile != null && tile.getType() == TileType.FOREST && tile.getResource() > 0) {
                    tile.setResource(tile.getResource() - 1);
                    world.setWood(world.getWood() + workYield(agent, GameConfig.WOOD_PER_LOG));
                    if (random.nextDouble() < 0.03) {
                        postEvent(new GameEvent(day, EventType.ECONOMY, "Axes in the forest",
                                agent.getCitizen().getName() + " brings back a load of wood.", 1));
                    }
                }
                break;
            case GATHERER:
                if (tile != null && tile.getType() == TileType.ORCHARD && tile.getResource() > 0) {
                    tile.setResource(tile.getResource() - 1);
                    world.setFood(world.getFood() + workYield(agent, GameConfig.FOOD_PER_FRUIT));
                    if (random.nextDouble() < 0.03) {
                        postEvent(new GameEvent(day, EventType.ECONOMY, "Fruit harvest",
                                agent.getCitizen().getName() + " fills a basket in the orchard.", 1));
                    }
                }
                break;
            case MINER:
                if (tile != null && (tile.getType() == TileType.MOUNTAIN || tile.getType() == TileType.CAVE)
                        && tile.getResource() > 0) {
                    tile.setResource(tile.getResource() - 1);
                    world.setStone(world.getStone() + workYield(agent, GameConfig.STONE_PER_DIG));
                    if (random.nextDouble() < 0.5) {
                        world.setOre(world.getOre() + GameConfig.ORE_PER_DIG);
                    }
                    if (random.nextDouble() < 0.06) {
                        world.setCrystal(world.getCrystal() + 1);
                    }
                    if (random.nextDouble() < 0.03) {
                        postEvent(new GameEvent(day, EventType.ECONOMY, "From the deep rock",
                                agent.getCitizen().getName() + " hauls stone out of the mountain.", 1));
                    }
                }
                break;
            case FISHER:
                if (map.hasWaterNeighbour(agent.getX(), agent.getY())) {
                    world.setFood(world.getFood() + workYield(agent, GameConfig.FOOD_PER_FISH));
                    if (random.nextDouble() < 0.03) {
                        postEvent(new GameEvent(day, EventType.ECONOMY, "The nets are full",
                                agent.getCitizen().getName() + " returns with fish from the shore.", 1));
                    }
                }
                break;
            case HUNTER: {
                Animal animal = agent.getTargetAnimal();
                if (animal != null && animal.isAlive()
                        && Math.abs(animal.getX() - agent.getX()) <= 1
                        && Math.abs(animal.getY() - agent.getY()) <= 1) {
                    animal.setHealth(animal.getHealth()
                            - (int) Math.round(workYield(agent, 6)));
                    if (animal.getType().isDangerous() && random.nextDouble() < 0.15) {
                        Citizen hunter = agent.getCitizen();
                        hunter.setHealth(hunter.getHealth() - 18);
                        if (hunter.getHealth() <= 0) {
                            killCitizenByCause(hunter, day, animal.getType().getDisplayName());
                            agent.clearTarget();
                            break;
                        }
                    }
                    if (animal.getHealth() <= 0) {
                        animal.setAlive(false);
                        world.setFood(world.getFood() + workYield(agent, GameConfig.FOOD_PER_HUNT));
                        postEvent(new GameEvent(day, EventType.ECONOMY, "A successful hunt",
                                agent.getCitizen().getName() + " brings down a "
                                        + animal.getType().getDisplayName().toLowerCase() + ".", 2));
                        agent.clearTarget();
                    }
                } else if (animal == null || !animal.isAlive()) {
                    agent.clearTarget();
                }
                break;
            }
            case BUILDER: {
                agent.setProgress(agent.getProgress() + 1);
                if (agent.getProgress() >= 6 && tile != null && tile.isFreeLand()) {
                    Structure structure = pickStructure();
                    if (world.getWood() >= structure.getWoodCost()
                            && world.getStone() >= structure.getStoneCost()) {
                        world.setWood(world.getWood() - structure.getWoodCost());
                        world.setStone(world.getStone() - structure.getStoneCost());
                        map.placeStructure(agent.getX(), agent.getY(), structure);
                        world.incrementStructuresBuilt();
                        postEvent(new GameEvent(day, EventType.ECONOMY, "New construction",
                                structure.getDisplayName() + " raised by "
                                        + agent.getCitizen().getName() + ".", 3));
                        agent.clearTarget();
                    } else {
                        agent.setProgress(3);
                    }
                }
                break;
            }
            case PRIEST:
                for (Citizen nearby : world.getLivingCitizens()) {
                    if (nearby != agent.getCitizen()
                            && near(agent, nearby, 4)) {
                        nearby.setHappiness(nearby.getHappiness() + 0.5);
                        nearby.setLoyalty(nearby.getLoyalty() + 0.3);
                        if (random.nextDouble() < 0.01) {
                            nearby.setReligion(agent.getCitizen().getReligion());
                        }
                    }
                }
                break;
            case RULER:
                for (Citizen nearby : world.getLivingCitizens()) {
                    if (near(agent, nearby, 5)) {
                        nearby.setLoyalty(nearby.getLoyalty() + 0.4);
                    }
                }
                break;
            case TRADER:
                world.setGold(world.getGold() + 1.5);
                break;
            case LABORER:
            default:
                if (tile != null && tile.getType() == TileType.GRASS) {
                    world.setFood(world.getFood() + 1);
                }
                break;
        }

        if (job.getTarget() != null && tile != null && tile.getResource() <= 0) {
            agent.clearTarget();
        }
    }

    private Structure pickStructure() {
        double roll = random.nextDouble();
        if (roll < 0.50) {
            return Structure.HUT;
        }
        if (roll < 0.65) {
            return Structure.DOCK;
        }
        if (roll < 0.80) {
            return Structure.TOWER;
        }
        if (roll < 0.90) {
            return Structure.TEMPLE;
        }
        return Structure.MINE;
    }

    private boolean near(Agent agent, Citizen citizen, int distance) {
        Agent other = world.findAgent(citizen);
        if (other == null) {
            return false;
        }
        return Math.abs(agent.getX() - other.getX()) <= distance
                && Math.abs(agent.getY() - other.getY()) <= distance;
    }

    private static int sign(int value) {
        return value > 0 ? 1 : -1;
    }

    // ------------------------------------------------------------------
    // Wildlife and ideas
    // ------------------------------------------------------------------

    private void stepAnimals() {
        IslandMap map = world.getMap();
        for (Animal animal : new ArrayList<>(world.getAnimals())) {
            if (!animal.isAlive()) {
                world.removeAnimal(animal);
                continue;
            }
            if (random.nextDouble() < 0.45) {
                int nx = animal.getX() + random.nextInt(3) - 1;
                int ny = animal.getY() + random.nextInt(3) - 1;
                TileType type = map.typeAt(nx, ny);
                boolean valid = animal.getType().isWater()
                        ? type == TileType.WATER
                        : type.isWalkable();
                if (valid) {
                    animal.setX(nx);
                    animal.setY(ny);
                }
            }
        }
        int alive = 0;
        for (Animal animal : world.getAnimals()) {
            if (animal.isAlive()) {
                alive++;
            }
        }
        if (alive < GameConfig.INITIAL_ANIMALS && random.nextDouble() < 0.06) {
            World.spawnAnimal(world, random);
        }
    }

    private void stepIdeas() {
        List<Agent> agents = new ArrayList<>(world.getAgents());
        for (Agent agent : agents) {
            Citizen citizen = agent.getCitizen();
            if (!citizen.isAlive()) {
                continue;
            }
            Idea idea = citizen.getIdea();
            citizen.setHappiness(citizen.getHappiness() + idea.getHappinessEffect() * 0.02);
            citizen.setLoyalty(citizen.getLoyalty() + idea.getLoyaltyEffect() * 0.02);

            if (random.nextDouble() < 0.02) {
                for (Agent other : agents) {
                    if (other != agent && other.getCitizen().isAlive()
                            && Math.abs(agent.getX() - other.getX()) <= 2
                            && Math.abs(agent.getY() - other.getY()) <= 2) {
                        other.getCitizen().setIdea(idea);
                        break;
                    }
                }
            }
            if (random.nextDouble() < 0.001) {
                citizen.setIdea(pick(Idea.values()));
            }
        }
    }

    // ------------------------------------------------------------------
    // People and politics
    // ------------------------------------------------------------------

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
        mood += Math.min(10, world.getMap().countStructures() * 0.8);
        mood += world.getWeather().getMoodDelta();
        mood += Age.of(world.getDay()).getMoodDelta();
        return clamp(mood * 0.6 + averageHappiness * 0.4);
    }

    private void stepCitizens(int day, double population, double mood) {
        for (Citizen citizen : new ArrayList<>(world.getCitizens())) {
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

            citizen.setHappiness(citizen.getHappiness()
                    + happinessDelta + citizen.getTrait().getHappinessModifier() * 0.02);
            citizen.setHealth(citizen.getHealth() + healthDelta);
            citizen.setWealth(citizen.getWealth() + wealthDelta);
            citizen.setLoyalty(citizen.getLoyalty()
                    + loyaltyDelta + citizen.getTrait().getLoyaltyModifier() * 0.02);
            citizen.setEducation(citizen.getEducation()
                    + random.nextDouble() * 0.05
                    + Math.max(0, citizen.getTrait().getLearningModifier()) * 0.04);

            if (citizen.isAdult() && citizen.getJob() == Job.LABORER
                    && citizen.getSocialClass() == SocialClass.CHILD) {
                Job grownJob = random.nextDouble() < 0.7
                        ? citizen.getRace().getPreferredJob()
                        : pick(Job.values());
                if (grownJob == Job.RULER) {
                    grownJob = Job.LABORER;
                }
                citizen.setJob(grownJob);
                citizen.setOccupation(grownJob.getDisplayName());
                citizen.setSocialClass(grownJob == Job.PRIEST
                        ? SocialClass.CLERGY
                        : (grownJob == Job.BUILDER || grownJob == Job.MINER
                        || grownJob == Job.TRADER || grownJob == Job.WOODCUTTER
                        ? SocialClass.ARTISAN : SocialClass.LABORER));
                Agent agent = world.findAgent(citizen);
                if (agent != null) {
                    agent.setJob(grownJob);
                }
                postEvent(new GameEvent(day, EventType.POLITICS, "Coming of age",
                        citizen.getName() + " has grown up and become a "
                                + grownJob.getDisplayName().toLowerCase() + ".", 2));
            }
            if (citizen.isAdult() && citizen.getWealth() > 75
                    && random.nextDouble() < 0.01) {
                citizen.setSocialClass(SocialClass.NOBLE);
            }
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
            deathChance *= 1.0 - Math.max(0, citizen.getTrait().getSurvivalModifier());
            if (random.nextDouble() < deathChance) {
                citizen.setAlive(false);
                citizen.setHealth(0);
                world.incrementDeaths();
                postEvent(new GameEvent(day, EventType.DEATH, "A life ends",
                        citizen.getName() + " has died at the age of " + ageYears + ".", 2));
            }
        }
    }

    /**
     * Births now take time: one woman carries one child for three seasons,
     * then rests for a full year. No instant population spikes.
     */
    private void stepBirths(int day, double mood) {
        if (world.getPopulation() >= GameConfig.MAX_POPULATION) {
            return;
        }

        // First, anybody whose nine months are over gives birth.
        for (Citizen mother : new ArrayList<>(world.getLivingCitizens())) {
            if (!mother.isAlive() || !mother.isPregnant() || !mother.isDue(day)) {
                continue;
            }
            deliverChild(day, mother);
            if (world.getPopulation() >= GameConfig.MAX_POPULATION) {
                return;
            }
        }

        // Then a small number of eligible women can begin a new pregnancy.
        double fertility = 0.35 + mood / 100.0;
        for (Citizen woman : new ArrayList<>(world.getLivingCitizens())) {
            if (!woman.canConceive(day)) {
                continue;
            }
            double chance = GameConfig.CONCEPTION_CHANCE_PER_DAY
                    * fertility
                    * Age.of(day).getBirthMultiplier()
                    * (0.5 + woman.getHealth() / 200.0)
                    * (woman.getWealth() < 15 ? 0.5 : 1.0);
            if (random.nextDouble() < chance) {
                woman.conceive(day);
                postEvent(new GameEvent(day, EventType.BIRTH, "Expecting",
                        woman.getName() + " is expecting. The child will arrive after three seasons.", 2));
            }
        }
    }

    private void deliverChild(int day, Citizen mother) {
        mother.deliver(day);
        Citizen child = new Citizen(
                world.nextId(),
                NameGenerator.fullName(random),
                random.nextBoolean() ? Gender.MALE : Gender.FEMALE,
                mother.getRace(),
                mother.getReligion(),
                pick(Ideology.values()),
                0
        );
        child.setJob(Job.LABORER);
        child.setOccupation(Job.LABORER.getDisplayName());
        child.setSocialClass(SocialClass.CHILD);
        child.setIdea(mother.getIdea());
        child.setHappiness(60);
        child.setHealth(70);
        child.setLoyalty(mother.getLoyalty());
        world.addCitizen(child);
        world.incrementBirths();

        Agent motherAgent = world.findAgent(mother);
        int x = motherAgent == null ? world.getMap().getWidth() / 2 : motherAgent.getX();
        int y = motherAgent == null ? world.getMap().getHeight() / 2 : motherAgent.getY();
        world.addAgent(new Agent(child, x, y, Job.LABORER));
        postEvent(new GameEvent(day, EventType.BIRTH, "New life",
                child.getName() + " was born to " + mother.getName()
                        + " after three seasons.", 2));
    }

    private void stepPolitics(int day, double mood) {
        Ruler ruler = world.getRuler();
        if (ruler == null || !ruler.getCitizen().isAlive()) {
            holdSuccession(day, "The throne is empty");
            return;
        }
        double approvalTarget = clamp(50 + (mood - 50) * 0.8);
        double approvalNow = ruler.getApproval();
        ruler.setApproval(approvalNow + (approvalTarget - approvalNow) * 0.05
                + random.nextDouble() * 0.4 - 0.2);

        boolean unstable = world.getStability() < 35 && ruler.getApproval() < 25;
        if (unstable && random.nextDouble() < 0.02) {
            postEvent(new GameEvent(day, EventType.COUP, "The throne is stormed",
                    "Soldiers surround the palace of " + ruler.getCitizen().getName()
                            + ". A coup is under way.", 5));
            world.setStability(world.getStability() - 15);
            holdSuccession(day, "A coup has overthrown " + ruler.getCitizen().getName());
        }
    }

    /** Choose a new leader from the living adults. */
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
        best.setJob(Job.RULER);
        best.setOccupation(Job.RULER.getDisplayName());
        best.setSocialClass(SocialClass.NOBLE);
        Agent agent = world.findAgent(best);
        if (agent != null) {
            agent.setJob(Job.RULER);
        }
        world.setRuler(new Ruler(best, GameConfig.RULER_TITLE, day));
        postEvent(new GameEvent(day, EventType.SUCCESSION, "A new ruler rises",
                (reason == null || reason.isEmpty() ? "The island needs a leader" : reason)
                        + ". " + best.getName() + " now holds the throne.", 4));
    }

    private void maybeGenerateEvent(int day) {
        if (random.nextDouble() > 0.09) {
            return;
        }
        switch (random.nextInt(12)) {
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
            case 1:
                world.setFood(world.getFood() - 35);
                postEvent(new GameEvent(day, EventType.DISASTER, "Drought",
                        "The rains fail. The storehouses lose 35 food.", 4));
                break;
            case 2:
                world.setGold(world.getGold() - 8);
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setHappiness(citizen.getHappiness() + 7);
                }
                postEvent(new GameEvent(day, EventType.FESTIVAL, "Lantern night",
                        "The village square glows with lanterns. Spirits are high.", 2));
                break;
            case 3:
                world.setGold(world.getGold() + 30);
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setWealth(citizen.getWealth() + 3);
                }
                postEvent(new GameEvent(day, EventType.ECONOMY, "A merchant ship arrives",
                        "A foreign trader docks with gold and strange goods.", 2));
                break;
            case 4:
                world.setStability(world.getStability() - 10);
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setHappiness(citizen.getHappiness() - 5);
                }
                postEvent(new GameEvent(day, EventType.UNREST, "Rumours in the market",
                        "Angry voices gather in the market square. Stability falls.", 3));
                break;
            case 5:
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setEducation(citizen.getEducation() + 6);
                }
                postEvent(new GameEvent(day, EventType.DISCOVERY, "A scholar's discovery",
                        "An old map is found in the ruins. The island learns its own past.", 2));
                break;
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
                    Race race = pick(Race.values());
                    Job job = race.getPreferredJob();
                    Citizen newcomer = new Citizen(
                            world.nextId(),
                            NameGenerator.fullName(random),
                            random.nextBoolean() ? Gender.MALE : Gender.FEMALE,
                            race,
                            pick(Religion.values()),
                            pick(Ideology.values()),
                            (18 + random.nextInt(25)) * GameConfig.DAYS_PER_YEAR
                    );
                    newcomer.setJob(job);
                    newcomer.setOccupation(job.getDisplayName());
                    newcomer.setIdea(pick(Idea.values()));
                    newcomer.setWealth(random.nextInt(40));
                    newcomer.setSocialClass(SocialClass.LABORER);
                    world.addCitizen(newcomer);
                    int[] spot = world.getMap().randomWalkable(random);
                    world.addAgent(new Agent(newcomer, spot[0], spot[1], job));
                }
                postEvent(new GameEvent(day, EventType.ECONOMY, "A raft from abroad",
                        arrivals + " newcomers wash onto the beach and ask to stay.", 3));
                break;
            }
            case 8: {
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
            case 9:
                world.setCrystal(world.getCrystal() + 2);
                postEvent(new GameEvent(day, EventType.DISCOVERY, "A crystal vein",
                        "Miners uncover a glittering seam deep under the hill.", 3));
                break;
            case 10: {
                int[] spot = world.getMap().randomTileOf(TileType.FOREST, random);
                if (spot != null) {
                    Tile tile = world.getMap().get(spot[0], spot[1]);
                    tile.setResource(0);
                }
                world.setWood(world.getWood() - 10);
                postEvent(new GameEvent(day, EventType.DISASTER, "Forest fire",
                        "Smoke rises over the trees. Ten stored logs are lost.", 4));
                break;
            }
            default: {
                int[] spot = world.getMap().randomTileOf(TileType.ORCHARD, random);
                if (spot != null) {
                    Tile tile = world.getMap().get(spot[0], spot[1]);
                    tile.setResource(tile.getResource() + 6);
                }
                postEvent(new GameEvent(day, EventType.ECONOMY, "The fruit bloom",
                        "A warm week fills the orchards with fruit.", 2));
                break;
            }
        }
    }

    // ------------------------------------------------------------------
    // World powers (player-triggered miracles and disasters)
    // ------------------------------------------------------------------

    public void castPower(Power power, int tx, int ty) {
        int day = world.getDay();
        int radius;
        switch (power) {
            case RAIN:
                radius = 4;
                for (int y = ty - radius; y <= ty + radius; y++) {
                    for (int x = tx - radius; x <= tx + radius; x++) {
                        Tile tile = world.getMap().get(x, y);
                        if (tile != null && (tile.getType() == TileType.FOREST
                                || tile.getType() == TileType.ORCHARD
                                || tile.getType() == TileType.MOUNTAIN
                                || tile.getType() == TileType.CAVE)) {
                            tile.setResource(Math.min(24, tile.getResource() + 9));
                        }
                    }
                }
                break;
            case BLOOM:
                radius = 7;
                for (int y = ty - radius; y <= ty + radius; y++) {
                    for (int x = tx - radius; x <= tx + radius; x++) {
                        Tile tile = world.getMap().get(x, y);
                        if (tile != null && tile.getType() == TileType.GRASS
                                && random.nextDouble() < 0.35) {
                            tile.setType(TileType.ORCHARD);
                            tile.setResource(8);
                        } else if (tile != null && (tile.getType() == TileType.ORCHARD
                                || tile.getType() == TileType.FOREST)) {
                            tile.setResource(Math.min(24, tile.getResource() + 6));
                        }
                    }
                }
                world.setFood(world.getFood() + 60);
                break;
            case HEAL:
                radius = 5;
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (nearAgent(citizen, tx, ty, radius)) {
                        citizen.setHealth(Math.min(100, citizen.getHealth() + 40));
                    }
                }
                break;
            case BLESS:
                radius = 6;
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (nearAgent(citizen, tx, ty, radius)) {
                        citizen.setHappiness(Math.min(100, citizen.getHappiness() + 18));
                        citizen.setLoyalty(Math.min(100, citizen.getLoyalty() + 14));
                    }
                }
                break;
            case LIGHTNING:
            case PLAGUE:
            case EARTHQUAKE:
            case METEOR:
                radius = power == Power.METEOR ? 4 : (power == Power.EARTHQUAKE ? 3 : 2);
                for (Citizen citizen : new ArrayList<>(world.getLivingCitizens())) {
                    if (!nearAgent(citizen, tx, ty, radius)) {
                        continue;
                    }
                    double damage = power == Power.METEOR ? 75
                            : (power == Power.EARTHQUAKE ? 22
                            : (power == Power.LIGHTNING ? 55 : 34));
                    citizen.setHealth(citizen.getHealth() - damage - random.nextDouble() * 10);
                    if (citizen.getHealth() <= 0) {
                        killCitizen(citizen, day, power);
                    }
                }
                for (Animal animal : new ArrayList<>(world.getAnimals())) {
                    if (animal.isAlive() && distance(animal.getX(), animal.getY(), tx, ty) <= radius) {
                        animal.setHealth(animal.getHealth() - 40);
                        if (animal.getHealth() <= 0) {
                            animal.setAlive(false);
                        }
                    }
                }
                for (int y = ty - radius; y <= ty + radius; y++) {
                    for (int x = tx - radius; x <= tx + radius; x++) {
                        Tile tile = world.getMap().get(x, y);
                        if (tile == null) {
                            continue;
                        }
                        if ((power == Power.EARTHQUAKE || power == Power.METEOR)
                                && tile.getStructure() != Structure.NONE) {
                            tile.setStructure(Structure.NONE);
                        }
                        if (power == Power.METEOR && distance(x, y, tx, ty) <= 1
                                && tile.getType() != TileType.WATER) {
                            tile.setType(TileType.MOUNTAIN);
                            tile.setResource(12);
                        }
                    }
                }
                if (power == Power.EARTHQUAKE || power == Power.METEOR) {
                    world.setStability(world.getStability() - (power == Power.METEOR ? 14 : 8));
                }
                if (power == Power.METEOR) {
                    world.setStone(world.getStone() + 60);
                    world.setOre(world.getOre() + 20);
                    world.setCrystal(world.getCrystal() + 5);
                }
                break;
            case PEACE:
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setHappiness(Math.min(100, citizen.getHappiness() + 10));
                    citizen.setLoyalty(Math.min(100, citizen.getLoyalty() + 8));
                }
                world.setStability(world.getStability() + 12);
                break;
            case PROSPERITY:
                world.setGold(world.getGold() + 180);
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setWealth(citizen.getWealth() + 18);
                }
                break;
            default:
                break;
        }
        postEvent(new GameEvent(day, EventType.POWER, "A hand from the sky",
                power.getDisplayName() + " was used at (" + tx + "," + ty + ").", 3));
    }

    private void killCitizen(Citizen citizen, int day, Power cause) {
        citizen.setAlive(false);
        citizen.setHealth(0);
        world.incrementDeaths();
        Agent agent = world.findAgent(citizen);
        if (agent != null) {
            world.removeAgent(agent);
        }
        if (citizen.getId() == (world.getRuler() == null ? -1
                : world.getRuler().getCitizen().getId())) {
            world.setRuler(null);
        }
        postEvent(new GameEvent(day, EventType.DISASTER, "A life cut short",
                citizen.getName() + " did not survive " + cause.getDisplayName() + ".", 3));
    }

    private boolean nearAgent(Citizen citizen, int x, int y, int radius) {
        Agent agent = world.findAgent(citizen);
        if (agent == null) {
            return false;
        }
        return distance(agent.getX(), agent.getY(), x, y) <= radius;
    }

    private static int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private <T> T pick(T[] values) {
        return values[random.nextInt(values.length)];
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }
}
