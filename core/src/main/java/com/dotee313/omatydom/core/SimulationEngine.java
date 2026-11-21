package com.dotee313.omatydom.core;

import com.dotee313.omatydom.core.civ.Achievement;
import com.dotee313.omatydom.core.civ.Clan;
import com.dotee313.omatydom.core.civ.Culture;
import com.dotee313.omatydom.core.civ.DiplomacyRelation;
import com.dotee313.omatydom.core.civ.Kingdom;
import com.dotee313.omatydom.core.civ.Species;
import com.dotee313.omatydom.core.civ.StatusEffect;
import com.dotee313.omatydom.core.civ.TradeRoute;
import com.dotee313.omatydom.core.civ.War;
import com.dotee313.omatydom.core.civ.WarType;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.ResourceType;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Idea;
import com.dotee313.omatydom.core.model.Ideology;
import com.dotee313.omatydom.core.model.Job;
import com.dotee313.omatydom.core.model.Race;
import com.dotee313.omatydom.core.model.Religion;
import com.dotee313.omatydom.core.model.Ruler;
import com.dotee313.omatydom.core.model.SocialClass;
import com.dotee313.omatydom.core.model.Trait;
import com.dotee313.omatydom.core.world.Agent;
import com.dotee313.omatydom.core.world.Animal;
import com.dotee313.omatydom.core.world.AnimalType;
import com.dotee313.omatydom.core.world.Biome;
import com.dotee313.omatydom.core.world.IslandMap;
import com.dotee313.omatydom.core.world.MapTemplate;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;
import com.dotee313.omatydom.core.world.Weather;
import com.dotee313.omatydom.core.world.WorldLaw;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Advances the island by one day at a time. Citizens physically walk the map,
 * harvest resources, raise buildings, hunt animals and trade ideas.
 */
public final class SimulationEngine {
    private static final int MAX_TRADE_ROUTES = 6;
    private static final int TRADE_INTERVAL_MIN_DAYS = 30;
    private static final int TRADE_INTERVAL_MAX_DAYS = 45;
    private static final int TRADE_DURATION_MIN_DAYS = 10;
    private static final int TRADE_DURATION_MAX_DAYS = 20;
    private static final String[] TRADE_GOODS = {
            "Grain", "Spice", "Iron", "Crystal", "Timber", "Salt", "Silk", "Wine", "Leather", "Herbs"
    };
    private static final int[] KINGDOM_COLORS = {
            0xFFFCA311, 0xFF4FC3F7, 0xFFFF8A80, 0xFF66BB6A, 0xFFCE93D8, 0xFF8D8884
    };
    private static final int MIN_CAPITAL_DISTANCE = 8;

    private final World world;
    private final Random random;
    private final List<SimulationListener> listeners = new ArrayList<>();
    private int nextTradeRouteId = 1;
    private int nextTradeRouteDay;

    public SimulationEngine(World world, Random random) {
        this.world = world;
        this.random = random;
        for (TradeRoute route : world.getTradeRoutes()) {
            nextTradeRouteId = Math.max(nextTradeRouteId, route.getId() + 1);
        }
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
        stepAnimals(day);
        stepMonsters(day);
        stepIdeas();

        double mood = computeMood();
        stepCitizens(day, population, mood);
        stepBirths(day, mood);
        stepPolitics(day, mood);
        stepCivilizations(day, mood);
        stepCultureAndTech(day);
        stepWorldLaws(day);
        checkAchievements(day);

        // Stability eases towards a target instead of drifting to a permanent zero.
        double stabilityTarget = clamp(50 + (mood - 50) * 0.7);
        double stabilityNow = world.getStability();
        world.setStability(stabilityNow + (stabilityTarget - stabilityNow) * 0.05
                + random.nextDouble() * 0.6 - 0.3);
        world.getMap().regenerate(random);
        maybeGenerateEvent(day);
        stepTradeRoutes(day);

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
        if (world.isLawEnabled(WorldLaw.PEACEFUL_MONSTERS)) {
            return;
        }
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
        if (world.isLawEnabled(WorldLaw.HUNGER)) {
            world.setFood(world.getFood() - population * GameConfig.FOOD_PER_CITIZEN);
        }

        if (world.isLawEnabled(WorldLaw.HUNGER)
                && before < population * GameConfig.FOOD_PER_CITIZEN) {
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
                boolean plants = type == TileType.FOREST || type == TileType.ORCHARD;
                boolean minerals = type == TileType.MOUNTAIN || type == TileType.CAVE;
                boolean growthAllowed = (plants && world.isLawEnabled(WorldLaw.GROW_GRASS))
                        || (minerals && world.isLawEnabled(WorldLaw.GROW_MINERALS));
                double biomeFactor = tile.getBiome().getRegrowthModifier();
                if (growthAllowed && random.nextDouble() < chance * biomeFactor / 4) {
                    tile.setResource(Math.min(24, tile.getResource() + 1));
                }
                if (world.isLawEnabled(WorldLaw.VEGETATION_SEEDS) && type == TileType.GRASS
                        && random.nextDouble() < 0.0015 * biomeFactor && tile.getStructure() == Structure.NONE) {
                    tile.setType(random.nextBoolean() ? TileType.FOREST : TileType.ORCHARD);
                    tile.setResource(6);
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
        if (job == Job.BUILDER || job == Job.ROAD_BUILDER || job == Job.SETTLER) {
            Tile tile = map.get(agent.getTargetX(), agent.getTargetY());
            return tile != null && tile.isFreeLand();
        }
        if (job.getTarget() != null) {
            Tile tile = map.get(agent.getTargetX(), agent.getTargetY());
            return tile != null && tile.getType() == job.getTarget() && tile.getResource() > 0;
        }
        if (job == Job.HUNTER) {
            Animal animal = agent.getTargetAnimal();
            return animal != null && animal.isAlive();
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
            case BLACKSMITH:
                setTerrainTarget(agent, TileType.MOUNTAIN);
                break;
            case SETTLER:
            case ROAD_BUILDER:
                setTerrainTarget(agent, TileType.GRASS);
                break;
            case CLEANER:
            case FIREMAN:
            case ATTACKER:
                if (random.nextDouble() < 0.25) {
                    int[] spot = map.randomWalkable(random);
                    agent.setTarget(spot[0], spot[1]);
                }
                break;
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

    /** Traits, biome, weather, age, temporary states and technology shape every yield. */
    private double workYield(Agent agent, double base) {
        Citizen citizen = agent.getCitizen();
        double factor = 1.0 + citizen.getTrait().getWorkModifier()
                + world.getTechBonus(TechEffect.WORK)
                + world.getModPack().getWorkModifier() - 1.0;
        factor *= world.getWeather().getWorkMultiplier();
        factor *= Age.of(world.getDay()).getWorkMultiplier();
        Tile tile = world.getMap().get(agent.getX(), agent.getY());
        if (tile != null) {
            factor *= tile.getBiome().getWorkModifier();
        }
        for (StatusEffect effect : citizen.getActiveStatuses()) {
            factor *= 1.0 + effect.getWorkModifier();
        }
        return Math.max(base * 0.15, base * factor);
    }

    /** Food-focused work also feels the local biome's fertility. */
    private double foodYield(Agent agent, double base) {
        Tile tile = world.getMap().get(agent.getX(), agent.getY());
        double fertility = tile == null ? 1.0 : tile.getBiome().getFoodModifier();
        return workYield(agent, base * fertility);
    }

    private void work(Agent agent, int day) {
        Job job = agent.getJob();
        IslandMap map = world.getMap();
        Tile tile = map.get(agent.getX(), agent.getY());

        switch (job) {
            case WOODCUTTER:
                if (tile != null && tile.getType() == TileType.FOREST && tile.getResource() > 0) {
                    tile.setResource(tile.getResource() - 1);
                    world.setWood(world.getWood() + workYield(agent,
                            GameConfig.WOOD_PER_LOG * (1.0 + world.getTechBonus(TechEffect.WOOD))));
                    if (random.nextDouble() < 0.03) {
                        postEvent(new GameEvent(day, EventType.ECONOMY, "Axes in the forest",
                                agent.getCitizen().getName() + " brings back a load of wood.", 1));
                    }
                }
                break;
            case GATHERER:
                if (tile != null && tile.getType() == TileType.ORCHARD && tile.getResource() > 0) {
                    tile.setResource(tile.getResource() - 1);
                    world.setFood(world.getFood() + foodYield(agent,
                            GameConfig.FOOD_PER_FRUIT * (1.0 + world.getTechBonus(TechEffect.FOOD))));
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
                    world.setStone(world.getStone() + workYield(agent,
                            GameConfig.STONE_PER_DIG * (1.0 + world.getTechBonus(TechEffect.STONE))));
                    if (random.nextDouble() < 0.5 + world.getTechBonus(TechEffect.ORE)) {
                        world.setOre(world.getOre() + GameConfig.ORE_PER_DIG);
                    }
                    if (random.nextDouble() < 0.06 + world.getTechBonus(TechEffect.ORE) * 0.3) {
                        world.setCrystal(world.getCrystal() + 1);
                    }
                    Tile here = map.get(agent.getX(), agent.getY());
                    if (here != null && here.getBiome() == Biome.CRYSTAL && random.nextDouble() < 0.08) {
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
                    double catchSize = foodYield(agent,
                            GameConfig.FOOD_PER_FISH * (1.0 + world.getTechBonus(TechEffect.FOOD)));
                    world.setFood(world.getFood() + catchSize);
                    world.addResource(ResourceType.FISH, catchSize / 6.0);
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
                        double meat = foodYield(agent,
                                GameConfig.FOOD_PER_HUNT * (1.0 + world.getTechBonus(TechEffect.FOOD)));
                        world.setFood(world.getFood() + meat);
                        world.addResource(ResourceType.MEAT, meat / 8.0);
                        world.addResource(ResourceType.BONES, 0.4);
                        world.addResource(ResourceType.LEATHER, 0.3);
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
                    double discount = Math.min(0.35, world.getTechBonus(TechEffect.BUILD_COST));
                    int woodCost = (int) Math.ceil(structure.getWoodCost() * (1.0 - discount));
                    int stoneCost = (int) Math.ceil(structure.getStoneCost() * (1.0 - discount));
                    if (world.getWood() >= woodCost && world.getStone() >= stoneCost) {
                        world.setWood(world.getWood() - woodCost);
                        world.setStone(world.getStone() - stoneCost);
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
                for (Culture culture : world.getCultures()) {
                    culture.setKnowledge(culture.getKnowledge()
                            + 0.08 + world.getTechBonus(TechEffect.CULTURE));
                }
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
                world.setGold(world.getGold() + 1.5
                        + world.getTechBonus(TechEffect.TRADE) * 2.0);
                for (Culture culture : world.getCultures()) {
                    culture.setKnowledge(culture.getKnowledge() + 0.03);
                }
                break;
            case BLACKSMITH:
                if (world.getOre() >= 0.4) {
                    world.setOre(world.getOre() - 0.4);
                    world.setGold(world.getGold() + 1.2
                            + world.getTechBonus(TechEffect.MILITARY) * 2.0);
                    world.addResource(ResourceType.IRON, 0.3);
                    if (random.nextDouble() < 0.04) {
                        for (Kingdom kingdom : world.getKingdoms()) {
                            kingdom.setArmy(kingdom.getArmy() + 1);
                        }
                    }
                }
                break;
            case SETTLER:
                if (tile != null && tile.getType() == TileType.GRASS && random.nextDouble() < 0.01) {
                    maybeFoundClan(agent.getCitizen(), day);
                    maybeFoundKingdom(agent.getCitizen(), day);
                }
                world.setFood(world.getFood() + foodYield(agent, 0.5));
                break;
            case ROAD_BUILDER:
                if (tile != null && tile.isFreeLand() && random.nextDouble() < 0.03
                        && world.getWood() >= Structure.ROAD.getWoodCost()) {
                    world.setWood(world.getWood() - Structure.ROAD.getWoodCost());
                    map.placeStructure(agent.getX(), agent.getY(), Structure.ROAD);
                    world.incrementStructuresBuilt();
                }
                world.setGold(world.getGold() + 0.2);
                break;
            case CLEANER:
                world.setStability(world.getStability() + 0.05);
                for (Citizen nearby : world.getLivingCitizens()) {
                    if (near(agent, nearby, 3)) {
                        nearby.setHappiness(nearby.getHappiness() + 0.08);
                        nearby.addStatus(StatusEffect.CAFFEINATED);
                    }
                }
                break;
            case FIREMAN:
                for (int y = agent.getY() - 1; y <= agent.getY() + 1; y++) {
                    for (int x = agent.getX() - 1; x <= agent.getX() + 1; x++) {
                        Tile burning = map.get(x, y);
                        if (burning != null) {
                            burning.setBiome(Biome.GRASS);
                        }
                    }
                }
                world.setStability(world.getStability() + 0.03);
                break;
            case ATTACKER:
                if (random.nextDouble() < 0.02) {
                    for (Kingdom kingdom : world.getKingdoms()) {
                        kingdom.setArmy(kingdom.getArmy() + 1);
                    }
                }
                world.setGold(world.getGold() + 0.3);
                break;
            case LABORER:
            default:
                if (tile != null && tile.getType() == TileType.GRASS) {
                    world.setFood(world.getFood() + foodYield(agent, 1));
                }
                break;
        }

        if (job.getTarget() != null && tile != null && tile.getResource() <= 0) {
            agent.clearTarget();
        }
    }

    private Structure pickStructure() {
        Structure[] basic = {Structure.HUT, Structure.HUT, Structure.FARM, Structure.MINE,
                Structure.DOCK, Structure.TOWER, Structure.TEMPLE, Structure.WELL};
        Structure[] advanced = {Structure.HALL, Structure.SMITHY, Structure.BARRACKS,
                Structure.GRANARY, Structure.WINDMILL, Structure.MARKET, Structure.LIBRARY,
                Structure.ROAD, Structure.GARDEN, Structure.WAREHOUSE};
        Structure[] pool = world.getTechsUnlocked() >= 8 ? advanced : basic;
        return pool[random.nextInt(pool.length)];
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

    private void stepAnimals(int day) {
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
        if (world.isLawEnabled(WorldLaw.ANIMALS_BABIES)
                && alive < GameConfig.INITIAL_ANIMALS && random.nextDouble() < 0.06) {
            World.spawnAnimal(world, random);
        }
        if (world.isLawEnabled(WorldLaw.ANIMALS_SPAWN)
                && alive < GameConfig.MAX_ANIMALS && day % 30 == 0 && random.nextDouble() < 0.25) {
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
        mood += world.getTechBonus(TechEffect.LOYALTY) * 4.0;
        if (world.isLawEnabled(WorldLaw.ANGRY_CIVILIANS)) {
            mood -= 8;
        }
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
            double statusMood = 0;
            double statusHealth = 0;
            for (StatusEffect effect : citizen.getActiveStatuses()) {
                statusMood += effect.getMoodModifier() * 0.05;
                statusHealth += effect.getHealthPerDay() * 0.25;
            }
            loyaltyDelta += world.getTechBonus(TechEffect.LOYALTY) * 0.2;

            citizen.setHappiness(citizen.getHappiness()
                    + happinessDelta + statusMood
                    + citizen.getTrait().getHappinessModifier() * 0.02);
            citizen.setHealth(citizen.getHealth() + healthDelta + statusHealth);
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
            if (random.nextDouble() < 0.0015 * world.getModPack().getTraitChanceModifier()) {
                citizen.setTrait(pick(Trait.values()));
            }
            citizen.tickStatuses();

            double ageYears = citizen.getAgeYears();
            double deathChance = 0.00015
                    + (world.isLawEnabled(WorldLaw.OLD_AGE)
                            ? Math.max(0, ageYears - 60) * 0.00006 : 0)
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
        int populationCap = world.isLawEnabled(WorldLaw.POPULATION_CAP)
                ? GameConfig.MAX_POPULATION : GameConfig.MAX_POPULATION + 40;
        if (!world.isLawEnabled(WorldLaw.CIV_BABIES)
                || world.getPopulation() >= populationCap) {
            return;
        }

        // First, anybody whose nine months are over gives birth.
        for (Citizen mother : new ArrayList<>(world.getLivingCitizens())) {
            if (!mother.isAlive() || !mother.isPregnant() || !mother.isDue(day)) {
                continue;
            }
            deliverChild(day, mother);
            if (world.getPopulation() >= populationCap) {
                return;
            }
        }

        // Then a small number of eligible women can begin a new pregnancy.
        double fertility = (0.35 + mood / 100.0)
                * world.getModPack().getBirthModifier()
                * (1.0 + world.getTechBonus(TechEffect.BIRTH));
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
                NameGenerator.personName(random, mother.getSpecies().getNamePoolKey()),
                random.nextBoolean() ? Gender.MALE : Gender.FEMALE,
                mother.getRace(),
                mother.getReligion(),
                pick(Ideology.values()),
                0
        );
        child.setSpecies(mother.getSpecies());
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
        if (world.isLawEnabled(WorldLaw.REBELLIONS)
                && unstable && random.nextDouble() < 0.02) {
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

    // ------------------------------------------------------------------
    // Civilizations, cultures, technology, world laws and achievements
    // ------------------------------------------------------------------

    /** Kingdoms, clans, relations and wars evolve on their own clocks. */
    private void stepCivilizations(int day, double mood) {
        for (Kingdom kingdom : world.getKingdoms()) {
            kingdom.setStability(kingdom.getStability()
                    + (mood - kingdom.getStability()) * 0.02);
            if (world.isLawEnabled(WorldLaw.CIV_ARMY) && random.nextDouble() < 0.08) {
                kingdom.setArmy(kingdom.getArmy() + 1);
            }
            if (world.isLawEnabled(WorldLaw.KINGDOM_EXPANSION) && day % 180 == 0
                    && kingdom.getTreasury() > 40) {
                kingdom.setTreasury(kingdom.getTreasury() - 40);
                int[] site = world.getMap().randomWalkable(random);
                if (site != null) {
                    kingdom.setCapitalX(site[0]);
                    kingdom.setCapitalY(site[1]);
                    postEvent(new GameEvent(day, EventType.KINGDOM, "A capital moves",
                            kingdom.getName() + " claims new ground at " + site[0] + "," + site[1]
                                    + ".", 3));
                }
            }
        }

        List<Kingdom> kingdoms = new ArrayList<>(world.getKingdoms());
        for (int i = 0; i < kingdoms.size(); i++) {
            for (int j = i + 1; j < kingdoms.size(); j++) {
                Kingdom a = kingdoms.get(i);
                Kingdom b = kingdoms.get(j);
                DiplomacyRelation relation = world.getRelation(a.getId(), b.getId());
                if (world.isLawEnabled(WorldLaw.DIPLOMACY) && day % 30 == 0
                        && random.nextDouble() < 0.20) {
                    double shift = random.nextDouble() < 0.5 ? -5 : 5;
                    if (relation == DiplomacyRelation.WAR) {
                        shift = 3;
                    }
                    DiplomacyRelation next = relation;
                    if (shift < 0 && relation == DiplomacyRelation.ALLIANCE) {
                        next = DiplomacyRelation.PEACE;
                    } else if (shift < 0 && relation == DiplomacyRelation.PEACE
                            && random.nextDouble() < 0.35) {
                        next = DiplomacyRelation.RIVALRY;
                    } else if (shift > 0 && relation == DiplomacyRelation.RIVALRY) {
                        next = DiplomacyRelation.PEACE;
                    } else if (shift > 0 && relation == DiplomacyRelation.PEACE
                            && random.nextDouble() < 0.25) {
                        next = DiplomacyRelation.ALLIANCE;
                    }
                    world.setRelation(a.getId(), b.getId(), next);
                    relation = next;
                }
                boolean canWar = !world.isLawEnabled(WorldLaw.FOREVER_PEACE)
                        && (relation == DiplomacyRelation.RIVALRY
                        || (relation == DiplomacyRelation.PEACE && random.nextDouble() < 0.4));
                if (canWar && random.nextDouble() < 0.015) {
                    startWar(day, pick(WarType.values()), a.getId(), b.getId());
                }
            }
        }

        for (War war : new ArrayList<>(world.getWars())) {
            war.setIntensity(Math.max(0, war.getIntensity() - 0.003 + random.nextDouble() * 0.004));
            Kingdom attacker = findKingdom(war.getAttackerId());
            Kingdom defender = findKingdom(war.getDefenderId());
            int armyLoss = (int) Math.ceil(war.getIntensity() * 1.2);
            int goldLoss = (int) Math.ceil(war.getIntensity() * 2.0);
            if (attacker != null) {
                attacker.setArmy(Math.max(0, attacker.getArmy() - armyLoss));
                attacker.setTreasury(Math.max(0, attacker.getTreasury() - goldLoss));
            }
            if (defender != null) {
                defender.setArmy(Math.max(0, defender.getArmy() - Math.max(1, armyLoss - 1)));
                defender.setTreasury(Math.max(0, defender.getTreasury() - Math.max(1, goldLoss - 1)));
            }
            if (world.isLawEnabled(WorldLaw.BORDER_STEALING) && random.nextDouble() < 0.02) {
                world.setGold(world.getGold() + 6);
            }
            boolean exhausted = war.getIntensity() <= 0.05 || day - war.getStartDay() > 120;
            if (exhausted) {
                world.getWars().remove(war);
                world.setRelation(war.getAttackerId(), war.getDefenderId(), DiplomacyRelation.PEACE);
                postEvent(new GameEvent(day, EventType.WAR, "A treaty is signed",
                        "The fighting between kingdom " + war.getAttackerId() + " and kingdom "
                                + war.getDefenderId() + " has ended.", 4));
            }
        }

        if (day % 90 == 0 && world.getClans().size() < 12 && world.getPopulation() > 12) {
            for (Citizen citizen : world.getLivingAdults()) {
                if (!belongsToClan(citizen)) {
                    foundClan(citizen, day);
                    break;
                }
            }
        }
        if (day % 240 == 0 && world.getKingdoms().size() < 6
                && world.getPopulation() >= 24 && world.isLawEnabled(WorldLaw.KINGDOM_EXPANSION)) {
            int before = world.getKingdoms().size();
            for (Citizen citizen : world.getLivingAdults()) {
                if (citizen.getJob() == Job.SETTLER || citizen.getJob() == Job.RULER) {
                    foundKingdom(citizen, day);
                    if (world.getKingdoms().size() > before) {
                        break;
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Trade caravans between kingdoms
    // ------------------------------------------------------------------

    /** Caravans open on a 30-45 day clock, travel 10-20 days, deliver, then get pruned. */
    private void stepTradeRoutes(int day) {
        for (TradeRoute route : world.getTradeRoutes()) {
            if (!route.isActive()
                    || findKingdom(route.getFromKingdomId()) == null
                    || findKingdom(route.getToKingdomId()) == null) {
                route.cancel();
                world.removeTradeRoute(route);
                continue;
            }
            if (route.advanceOneDay()) {
                finishTradeRoute(day, route);
                world.removeTradeRoute(route);
            }
        }

        if (nextTradeRouteDay <= 0) {
            nextTradeRouteDay = scheduleTradeDay(day);
        }
        if (day < nextTradeRouteDay) {
            return;
        }
        nextTradeRouteDay = scheduleTradeDay(day);

        int kingdomCount = world.getKingdoms().size();
        if (kingdomCount < 2) {
            return;
        }
        int slots = MAX_TRADE_ROUTES - world.getTradeRoutes().size();
        int openings = Math.min(slots, Math.max(1, kingdomCount - 1));
        for (int i = 0; i < openings; i++) {
            openTradeRoute(day);
        }
    }

    private int scheduleTradeDay(int day) {
        return day + TRADE_INTERVAL_MIN_DAYS
                + random.nextInt(TRADE_INTERVAL_MAX_DAYS - TRADE_INTERVAL_MIN_DAYS + 1);
    }

    /** Pick two distinct kingdoms and launch one caravan if the pair is not already trading. */
    private void openTradeRoute(int day) {
        List<Kingdom> kingdoms = world.getKingdoms();
        if (kingdoms.size() < 2) {
            return;
        }
        int size = kingdoms.size();
        int first = random.nextInt(size);
        int second = random.nextInt(size - 1);
        if (second >= first) {
            second++;
        }
        Kingdom from = kingdoms.get(first);
        Kingdom to = kingdoms.get(second);
        for (TradeRoute route : world.getTradeRoutes()) {
            if (sameKingdoms(route, from.getId(), to.getId())) {
                return;
            }
        }

        String good = TRADE_GOODS[random.nextInt(TRADE_GOODS.length)];
        int duration = TRADE_DURATION_MIN_DAYS
                + random.nextInt(TRADE_DURATION_MAX_DAYS - TRADE_DURATION_MIN_DAYS + 1);
        double reward = 10.0 + random.nextInt(21);
        TradeRoute route = new TradeRoute(nextTradeRouteId++, from.getId(), to.getId(),
                good, reward, duration);
        world.addTradeRoute(route);
        postEvent(new GameEvent(day, EventType.ECONOMY, "A trade route opens",
                "Merchants of " + from.getName() + " and " + to.getName()
                        + " agree to trade " + good.toLowerCase() + ".", 2));
    }

    private static boolean sameKingdoms(TradeRoute route, int a, int b) {
        return (route.getFromKingdomId() == a && route.getToKingdomId() == b)
                || (route.getFromKingdomId() == b && route.getToKingdomId() == a);
    }

    /** Pay gold, food and knowledge when a caravan reaches its destination. */
    private void finishTradeRoute(int day, TradeRoute route) {
        Kingdom from = findKingdom(route.getFromKingdomId());
        Kingdom to = findKingdom(route.getToKingdomId());
        double gold = route.getReward();
        world.setGold(world.getGold() + gold);
        world.setFood(world.getFood() + gold * 0.8);
        for (Kingdom kingdom : new Kingdom[]{from, to}) {
            if (kingdom != null) {
                kingdom.setTreasury(kingdom.getTreasury() + (int) Math.round(gold / 2.0));
            }
        }
        for (Culture culture : world.getCultures()) {
            culture.setKnowledge(culture.getKnowledge() + gold * 0.05);
        }
        world.incrementCaravansCompleted();
        String fromName = from == null ? "kingdom " + route.getFromKingdomId() : from.getName();
        String toName = to == null ? "kingdom " + route.getToKingdomId() : to.getName();
        postEvent(new GameEvent(day, EventType.ECONOMY, "A caravan arrives",
                "The " + route.getGood().toLowerCase() + " caravan from " + fromName
                        + " reached " + toName + ", earning " + (int) Math.round(gold) + " gold.", 3));
    }

    private void maybeFoundClan(Citizen citizen, int day) {
        if (world.getClans().size() < 12 && !belongsToClan(citizen)) {
            foundClan(citizen, day);
        }
    }

    private void maybeFoundKingdom(Citizen citizen, int day) {
        if (world.getKingdoms().size() >= 6 || world.getPopulation() < 24
                || !world.isLawEnabled(WorldLaw.KINGDOM_EXPANSION)) {
            return;
        }
        int before = world.getKingdoms().size();
        for (Citizen candidate : world.getLivingAdults()) {
            foundKingdom(candidate, day);
            if (world.getKingdoms().size() > before) {
                break;
            }
        }
    }

    public void foundClan(Citizen founder, int day) {
        int kingdomId = world.getKingdoms().isEmpty() ? 0 : world.getKingdoms().get(0).getId();
        Clan clan = new Clan(world.getClans().size() + 1,
                NameGenerator.clanName(random), kingdomId, founder.getId());
        clan.addMember(founder.getId());
        world.getClans().add(clan);
        world.incrementClansFounded();
        postEvent(new GameEvent(day, EventType.CLAN, "A clan is founded",
                founder.getName() + " gathers a following called " + clan.getName() + ".", 3));
    }

    public void foundKingdom(Citizen founder, int day) {
        if (founder == null || world.getKingdoms().size() >= KINGDOM_COLORS.length) {
            return;
        }
        Agent seat = world.findAgent(founder);
        int x = seat == null ? world.getMap().getWidth() / 2 : seat.getX();
        int y = seat == null ? world.getMap().getHeight() / 2 : seat.getY();
        for (Kingdom existing : world.getKingdoms()) {
            int dx = existing.getCapitalX() - x;
            int dy = existing.getCapitalY() - y;
            if (dx * dx + dy * dy < MIN_CAPITAL_DISTANCE * MIN_CAPITAL_DISTANCE) {
                return;
            }
        }
        Species species = founder.getSpecies();
        int id = world.getKingdoms().size() + 1;
        Kingdom kingdom = new Kingdom(id, NameGenerator.kingdomName(random), species,
                KINGDOM_COLORS[(id - 1) % KINGDOM_COLORS.length], x, y);
        kingdom.setStability(60);
        kingdom.setArmy(4);
        kingdom.setTreasury(50);
        world.getKingdoms().add(kingdom);
        world.incrementKingdomsFounded();
        postEvent(new GameEvent(day, EventType.KINGDOM, "A kingdom rises",
                "Settlers found " + kingdom.getName() + ".", 4));
    }

    private boolean belongsToClan(Citizen citizen) {
        for (Clan clan : world.getClans()) {
            if (clan.getMemberIds().contains(citizen.getId())) {
                return true;
            }
        }
        return false;
    }

    private Kingdom findKingdom(int id) {
        for (Kingdom kingdom : world.getKingdoms()) {
            if (kingdom.getId() == id) {
                return kingdom;
            }
        }
        return null;
    }

    /** Start one of the five war types with a unique id and fresh intensity. */
    public void startWar(int day, WarType type, int attackerId, int defenderId) {
        if (type == null || attackerId == defenderId) {
            return;
        }
        War war = new War(world.getWars().size() + 1, type, attackerId, defenderId, day);
        war.setIntensity(0.7 + random.nextDouble() * 0.3);
        world.getWars().add(war);
        world.incrementWarsStarted();
        world.setRelation(attackerId, defenderId, DiplomacyRelation.WAR);
        postEvent(new GameEvent(day, EventType.WAR, "War is declared",
                type.getDisplayName() + " between kingdom " + attackerId + " and kingdom "
                        + defenderId + " begins.", 5));
    }

    /** Culture accumulates knowledge and unlocks the next original technology. */
    private void stepCultureAndTech(int day) {
        if (world.getCultures().isEmpty()) {
            world.getCultures().add(new Culture(1, NameGenerator.cultureName(random)));
        }
        Culture culture = world.getCultures().get(0);
        double education = 0;
        List<Citizen> living = world.getLivingCitizens();
        for (Citizen citizen : living) {
            education += citizen.getEducation();
        }
        double average = living.isEmpty() ? 0 : education / living.size();
        culture.setKnowledge(culture.getKnowledge()
                + (0.01 + average * 0.0015) * (1.0 + world.getTechBonus(TechEffect.LEARNING)));

        Technology[] techs = Technology.values();
        int index = world.getTechsUnlocked();
        if (index >= techs.length) {
            return;
        }
        Technology next = techs[index];
        double goldCost = next.getCost() / 4.0;
        if (culture.getKnowledge() >= next.getCost() && world.getGold() >= goldCost) {
            culture.setKnowledge(culture.getKnowledge() - next.getCost());
            world.setGold(world.getGold() - goldCost);
            culture.addTech(next.name());
            world.incrementTechsUnlocked();
            world.addTechBonus(next.getEffect(), next.getMagnitude());
            postEvent(new GameEvent(day, EventType.TECH, "A new art is learned",
                    culture.getName() + " masters " + next.getDisplayName() + " ("
                            + next.getArabicName() + ").", 4));
        }
    }

    /** Per-tile effects of the twenty-three toggleable world laws. */
    private void stepWorldLaws(int day) {
        IslandMap map = world.getMap();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = map.get(x, y);
                if (tile == null || tile.getType() == TileType.WATER) {
                    continue;
                }
                if (world.isLawEnabled(WorldLaw.FOREVER_LAVA)
                        && (tile.getType() == TileType.MOUNTAIN || tile.getType() == TileType.CAVE)
                        && random.nextDouble() < 0.001) {
                    tile.setBiome(Biome.INFERNAL);
                    if (tile.getStructure() != Structure.NONE && random.nextDouble() < 0.3) {
                        tile.setStructure(Structure.NONE);
                    }
                }
                if (world.isLawEnabled(WorldLaw.FOREVER_TUMOR)
                        && tile.getType() == TileType.FOREST && random.nextDouble() < 0.0008) {
                    tile.setBiome(Biome.TUMOR);
                }
                if (world.isLawEnabled(WorldLaw.BIOME_OVERGROWTH)
                        && tile.getType() == TileType.GRASS && random.nextDouble() < 0.0005) {
                    tile.setBiome(pick(Biome.values()));
                }
                if (world.isLawEnabled(WorldLaw.EROSION)
                        && tile.getType() == TileType.FOREST && random.nextDouble() < 0.0002) {
                    tile.setType(TileType.GRASS);
                    tile.setResource(0);
                }
            }
        }
        for (Kingdom kingdom : world.getKingdoms()) {
            if (world.isLawEnabled(WorldLaw.CIV_ARMY) && random.nextDouble() < 0.01) {
                kingdom.setArmy(kingdom.getArmy() + 1);
            }
        }
        if (world.isLawEnabled(WorldLaw.FOREVER_PEACE) && !world.getWars().isEmpty()) {
            world.getWars().clear();
            for (Kingdom kingdom : world.getKingdoms()) {
                world.setRelation(kingdom.getId(), 0, DiplomacyRelation.PEACE);
            }
        }
    }

    private void checkAchievements(int day) {
        unlock(Achievement.FIRST_BLOOD, world.getBirths() >= 1);
        unlock(Achievement.BUILDER, world.getStructuresBuilt() >= 25);
        unlock(Achievement.POPULATION_50, world.getPopulation() >= 50);
        unlock(Achievement.LONG_RAIN, world.getWeatherDaysLeft() >= 5);
        unlock(Achievement.METEOR_MASTER, world.getPowersUsed() >= 10 && world.getCrystal() >= 20);
        unlock(Achievement.PEACEKEEPER, world.getWars().isEmpty() && world.getStability() >= 80);
        unlock(Achievement.WARLORD, world.getWarsStarted() >= 3);
        unlock(Achievement.SCHOLAR, world.getTechsUnlocked() >= 20);
        unlock(Achievement.CLAN_FOUNDER, world.getClansFounded() >= 5);
        unlock(Achievement.KINGDOM_FOUNDER, world.getKingdomsFounded() >= 4);
        unlock(Achievement.SURVIVOR, day >= GameConfig.DAYS_PER_YEAR * 10);
        unlock(Achievement.CENTURY, day >= GameConfig.DAYS_PER_YEAR * 100);
    }

    private void unlock(Achievement achievement, boolean condition) {
        if (!condition || !world.unlockAchievement(achievement)) {
            return;
        }
        postEvent(new GameEvent(world.getDay(), EventType.ACHIEVEMENT, "Achievement unlocked",
                achievement.getDisplayName() + " — " + achievement.getDescription(), 5));
    }

    private DisasterType[] disasterPool(boolean nature, boolean other) {
        List<DisasterType> pool = new ArrayList<>();
        for (DisasterType type : DisasterType.values()) {
            boolean natural = type == DisasterType.DRAGON_FLIGHT
                    || type == DisasterType.EARTHQUAKE || type == DisasterType.HEATWAVE
                    || type == DisasterType.METEORITE || type == DisasterType.TORNADO
                    || type == DisasterType.ACID_RAIN || type == DisasterType.ASH_FALL
                    || type == DisasterType.LAVA_FLOW || type == DisasterType.WILD_STAMPEDE
                    || type == DisasterType.BIOMASS_BLOOM || type == DisasterType.TUMOR_SPREAD;
            if ((natural && nature) || (!natural && other)) {
                pool.add(type);
            }
        }
        if (pool.isEmpty()) {
            return new DisasterType[]{DisasterType.EARTHQUAKE};
        }
        return pool.toArray(new DisasterType[0]);
    }

    /** Apply one of the seventeen world-log disasters with a real gameplay effect. */
    public void triggerDisaster(int day, DisasterType type) {
        IslandMap map = world.getMap();
        int[] center = map.randomWalkable(random);
        int cx = center == null ? map.getWidth() / 2 : center[0];
        int cy = center == null ? map.getHeight() / 2 : center[1];

        switch (type) {
            case ALIEN_VESSLE:
                world.addAnimal(new Animal(AnimalType.DRAGON, cx, cy));
                world.setGold(world.getGold() + 30);
                world.setCrystal(world.getCrystal() + 4);
                break;
            case BANDIT_RAID:
                world.setGold(world.getGold() - 15);
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setWealth(citizen.getWealth() - 10);
                    citizen.setHappiness(citizen.getHappiness() - 4);
                }
                for (Kingdom kingdom : world.getKingdoms()) {
                    kingdom.setArmy(Math.max(0, kingdom.getArmy() - 2));
                }
                break;
            case BIOMASS_BLOOM:
                for (int y = cy - 3; y <= cy + 3; y++) {
                    for (int x = cx - 3; x <= cx + 3; x++) {
                        Tile tile = map.get(x, y);
                        if (tile != null && tile.getType() == TileType.GRASS
                                && random.nextDouble() < 0.35) {
                            tile.setType(TileType.FOREST);
                            tile.setBiome(Biome.BIOMASS);
                            tile.setResource(12);
                        }
                    }
                }
                world.setFood(world.getFood() + 20);
                break;
            case DRAGON_FLIGHT:
                world.addAnimal(new Animal(AnimalType.DRAGON, cx, cy));
                world.setStability(world.getStability() - 6);
                break;
            case EARTHQUAKE:
                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        Tile tile = map.get(x, y);
                        if (tile != null && tile.getStructure() != Structure.NONE
                                && random.nextDouble() < 0.04) {
                            tile.setStructure(Structure.NONE);
                        }
                    }
                }
                world.setStability(world.getStability() - 8);
                break;
            case EVIL_MAGE:
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (random.nextDouble() < 0.08) {
                        citizen.setTrait(pick(Trait.values()));
                        citizen.setEducation(citizen.getEducation() - 5);
                        citizen.addStatus(StatusEffect.POISONED);
                    }
                }
                break;
            case HEATWAVE:
                world.setFood(world.getFood() - 25);
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setHappiness(citizen.getHappiness() - 3);
                }
                break;
            case HELLSPAWN:
                world.addAnimal(new Animal(AnimalType.ZOMBIE, cx, cy));
                world.addAnimal(new Animal(AnimalType.GOLEM, cx, cy));
                world.addAnimal(new Animal(AnimalType.WOLF, cx, cy));
                break;
            case METEORITE:
                for (int y = cy - 3; y <= cy + 3; y++) {
                    for (int x = cx - 3; x <= cx + 3; x++) {
                        Tile tile = map.get(x, y);
                        if (tile == null) {
                            continue;
                        }
                        if (distance(x, y, cx, cy) <= 1 && tile.getType() != TileType.WATER) {
                            tile.setType(TileType.MOUNTAIN);
                            tile.setBiome(Biome.INFERNAL);
                            tile.setResource(12);
                        }
                        tile.setStructure(Structure.NONE);
                    }
                }
                world.setStone(world.getStone() + 30);
                world.setOre(world.getOre() + 12);
                world.setStability(world.getStability() - 12);
                break;
            case TORNADO:
                for (int y = cy - 1; y <= cy + 1; y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        Tile tile = map.get(x, y);
                        if (tile != null && tile.getStructure() != Structure.NONE) {
                            tile.setStructure(Structure.NONE);
                        }
                    }
                }
                break;
            case TUMOR_SPREAD:
                for (int y = cy - 4; y <= cy + 4; y++) {
                    for (int x = cx - 4; x <= cx + 4; x++) {
                        Tile tile = map.get(x, y);
                        if (tile != null && tile.getType() != TileType.WATER
                                && random.nextDouble() < 0.3) {
                            tile.setBiome(Biome.TUMOR);
                        }
                    }
                }
                for (Citizen citizen : world.getLivingCitizens()) {
                    citizen.setHealth(citizen.getHealth() - 6);
                }
                break;
            case NECROMANCER_RISE:
                world.addAnimal(new Animal(AnimalType.ZOMBIE, cx, cy));
                world.addAnimal(new Animal(AnimalType.ZOMBIE, cx, cy));
                world.addAnimal(new Animal(AnimalType.ZOMBIE, cx, cy));
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (random.nextDouble() < 0.05) {
                        killCitizenByCause(citizen, day, "the risen dead");
                    }
                }
                break;
            case ACID_RAIN:
                for (int y = cy - 3; y <= cy + 3; y++) {
                    for (int x = cx - 3; x <= cx + 3; x++) {
                        Tile tile = map.get(x, y);
                        if (tile != null && tile.getType() != TileType.WATER) {
                            tile.setResource(Math.max(0, tile.getResource() - 4));
                        }
                    }
                }
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (nearAgent(citizen, cx, cy, 5)) {
                        citizen.addStatus(StatusEffect.POISONED);
                    }
                }
                break;
            case ASH_FALL:
                for (int y = cy - 4; y <= cy + 4; y++) {
                    for (int x = cx - 4; x <= cx + 4; x++) {
                        Tile tile = map.get(x, y);
                        if (tile != null && tile.getType() != TileType.WATER) {
                            tile.setBiome(Biome.WASTELAND);
                        }
                    }
                }
                break;
            case LAVA_FLOW:
                for (int y = cy - 3; y <= cy + 3; y++) {
                    for (int x = cx - 3; x <= cx + 3; x++) {
                        Tile tile = map.get(x, y);
                        if (tile != null && tile.getType() != TileType.WATER
                                && random.nextDouble() < 0.4) {
                            tile.setBiome(Biome.INFERNAL);
                            tile.setStructure(Structure.NONE);
                        }
                    }
                }
                break;
            case PLAGUE_SHIP:
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (random.nextDouble() < 0.2) {
                        citizen.addStatus(StatusEffect.COUGH);
                        citizen.addStatus(StatusEffect.ASH_FEVER);
                    }
                }
                world.setFood(world.getFood() - 10);
                break;
            case WILD_STAMPEDE:
                for (Agent agent : new ArrayList<>(world.getAgents())) {
                    if (nearAgent(agent.getCitizen(), cx, cy, 5)) {
                        agent.getCitizen().setHealth(agent.getCitizen().getHealth() - 12);
                    }
                }
                World.spawnAnimal(world, random);
                World.spawnAnimal(world, random);
                break;
            default:
                break;
        }
        postEvent(new GameEvent(day, EventType.DISASTER, type.getDisplayName(),
                type.getArabicName() + " strikes near " + cx + "," + cy + ".", 5));
    }

    private void maybeGenerateEvent(int day) {
        boolean natureDisasters = world.isLawEnabled(WorldLaw.DISASTERS_NATURE);
        boolean otherDisasters = world.isLawEnabled(WorldLaw.DISASTERS_OTHER);
        double chance = natureDisasters || otherDisasters ? 0.14 : 0.09;
        if (random.nextDouble() > chance) {
            return;
        }
        if ((natureDisasters || otherDisasters) && random.nextDouble() < 0.5) {
            DisasterType[] pool = disasterPool(natureDisasters, otherDisasters);
            triggerDisaster(day, pool[random.nextInt(pool.length)]);
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
                if (natureDisasters) {
                    world.setFood(world.getFood() - 35);
                    postEvent(new GameEvent(day, EventType.DISASTER, "Drought",
                            "The rains fail. The storehouses lose 35 food.", 4));
                }
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

    /** Spawn any of the catalogued creatures at a fitting habitat. */
    public void spawnCreature(AnimalType type) {
        if (type == null) {
            return;
        }
        int[] spot;
        if (type.isWater()) {
            spot = world.getMap().randomTileOf(TileType.WATER, random);
        } else if (type == AnimalType.DRAGON || type == AnimalType.GOLEM) {
            spot = world.getMap().randomTileOf(random.nextBoolean()
                    ? TileType.MOUNTAIN : TileType.CAVE, random);
        } else {
            spot = world.getMap().randomWalkable(random);
        }
        if (spot == null) {
            return;
        }
        world.addAnimal(new Animal(type, spot[0], spot[1]));
        postEvent(new GameEvent(world.getDay(), EventType.DISASTER, "A creature arrives",
                type.getDisplayName() + " appears at " + spot[0] + "," + spot[1] + ".", 3));
    }

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
                        citizen.removeStatus(StatusEffect.BURNING);
                        citizen.removeStatus(StatusEffect.POISONED);
                        citizen.removeStatus(StatusEffect.ASH_FEVER);
                        citizen.addStatus(StatusEffect.SHIELD);
                    }
                }
                break;
            case BLESS:
                radius = 6;
                for (Citizen citizen : world.getLivingCitizens()) {
                    if (nearAgent(citizen, tx, ty, radius)) {
                        citizen.setHappiness(Math.min(100, citizen.getHappiness() + 18));
                        citizen.setLoyalty(Math.min(100, citizen.getLoyalty() + 14));
                        citizen.addStatus(StatusEffect.ENCHANTED);
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
                    if (power == Power.PLAGUE) {
                        citizen.addStatus(random.nextBoolean()
                                ? StatusEffect.ASH_FEVER : StatusEffect.COUGH);
                    } else if (power == Power.LIGHTNING || power == Power.METEOR) {
                        citizen.addStatus(StatusEffect.BURNING);
                    } else if (power == Power.EARTHQUAKE) {
                        citizen.addStatus(StatusEffect.SLOWNESS);
                    }
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
