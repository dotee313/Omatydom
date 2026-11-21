package com.dotee313.omatydom.core;

import com.dotee313.omatydom.core.civ.Clan;
import com.dotee313.omatydom.core.civ.Culture;
import com.dotee313.omatydom.core.civ.DiplomacyRelation;
import com.dotee313.omatydom.core.civ.Kingdom;
import com.dotee313.omatydom.core.civ.Species;
import com.dotee313.omatydom.core.civ.TradeRoute;
import com.dotee313.omatydom.core.civ.War;
import com.dotee313.omatydom.core.civ.WarType;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Ideology;
import com.dotee313.omatydom.core.model.Job;
import com.dotee313.omatydom.core.model.Race;
import com.dotee313.omatydom.core.model.Religion;
import com.dotee313.omatydom.core.model.Ruler;
import com.dotee313.omatydom.core.model.SocialClass;
import com.dotee313.omatydom.core.world.Biome;
import com.dotee313.omatydom.core.world.Agent;
import com.dotee313.omatydom.core.world.Animal;
import com.dotee313.omatydom.core.world.AnimalType;
import com.dotee313.omatydom.core.world.IslandMap;
import com.dotee313.omatydom.core.world.MapTemplate;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;
import com.dotee313.omatydom.core.world.WorldLaw;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
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
    private double dayAccumulator;
    private final Map<Power, Integer> powerReadyDay = new EnumMap<>(Power.class);

    private OmatydomGame(World world, Random random) {
        this.world = world;
        this.random = random;
        this.engine = new SimulationEngine(world, random);
    }

    /** Create a reproducible island from a seed. */
    public static OmatydomGame create(long seed) {
        return create(seed, MapTemplate.ISLANDS, ModPack.CORE);
    }

    /** Create a reproducible island from a seed, template and ruleset. */
    public static OmatydomGame create(long seed, MapTemplate template, ModPack modPack) {
        Random random = new Random(seed);
        World world = World.create("Omatydom", GameConfig.INITIAL_POPULATION, random, template);
        world.setModPack(modPack);
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

    public IslandMap getMap() {
        return world.getMap();
    }

    public List<Agent> getAgents() {
        return world.getAgents();
    }

    public List<Animal> getAnimals() {
        return world.getAnimals();
    }

    // ------------------------------------------------------------------
    // Time control
    // ------------------------------------------------------------------

    /**
     * Advance real time. At speed x1 one full game year takes exactly one hour:
     * 360 days × 10 seconds. The Android UI calls this every 500 ms.
     */
    public void tick() {
        if (paused) {
            return;
        }
        dayAccumulator += (GameConfig.UI_TICK_MILLIS * (double) speed)
                / GameConfig.MILLIS_PER_GAME_DAY;
        while (dayAccumulator >= 1.0) {
            engine.advanceDay();
            dayAccumulator -= 1.0;
        }
    }

    /** Advance the simulation by an exact number of game days (CLI / tests). */
    public void advanceDays(int days) {
        for (int i = 0; i < days; i++) {
            engine.advanceDay();
        }
    }

    // ------------------------------------------------------------------
    // World powers
    // ------------------------------------------------------------------

    /** Cast a world power at a chosen tile. Costs gold and has a cooldown. */
    public boolean usePower(Power power, int x, int y) {
        if (power == null || !world.getMap().inBounds(x, y)) {
            return false;
        }
        int day = world.getDay();
        Integer ready = powerReadyDay.get(power);
        if (ready != null && day < ready) {
            return false;
        }
        if (world.getGold() < power.getGoldCost()) {
            return false;
        }
        world.setGold(world.getGold() - power.getGoldCost());
        powerReadyDay.put(power, day + power.getCooldownDays());
        world.incrementPowersUsed();
        engine.castPower(power, x, y);
        return true;
    }

    /** Convenience for the CLI and quick-play buttons: pick a random land tile. */
    public boolean usePower(Power power) {
        int[] spot = world.getMap().randomWalkable(random);
        return spot != null && usePower(power, spot[0], spot[1]);
    }

    public int getPowerReadyDay(Power power) {
        Integer ready = powerReadyDay.get(power);
        return ready == null ? 0 : ready;
    }

    public int getPowerCooldownDaysLeft(Power power) {
        return Math.max(0, getPowerReadyDay(power) - world.getDay());
    }

    /** 0..1 progress towards the next game day, for the UI clock bar. */
    public double getDayProgress() {
        return Math.max(0.0, Math.min(1.0, dayAccumulator));
    }

    public static int seasonOf(int day) {
        return ((Math.max(1, day) - 1) / GameConfig.DAYS_PER_SEASON) % 4;
    }

    public static int monthOf(int day) {
        return ((Math.max(1, day) - 1) / GameConfig.DAYS_PER_MONTH) % 12 + 1;
    }

    public static Age ageOf(int day) {
        return Age.of(day);
    }

    public Age getAge() {
        return Age.of(world.getDay());
    }

    public int getAgeProgress() {
        return (Math.max(1, world.getDay()) - 1) % GameConfig.DAYS_PER_AGE + 1;
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
        if (old != null && old.getCitizen() != chosen && old.getCitizen().isAlive()) {
            old.getCitizen().setJob(Job.LABORER);
            old.getCitizen().setOccupation(Job.LABORER.getDisplayName());
            old.getCitizen().setSocialClass(SocialClass.LABORER);
            Agent oldAgent = world.findAgent(old.getCitizen());
            if (oldAgent != null) {
                oldAgent.setJob(Job.LABORER);
            }
        }
        chosen.setJob(Job.RULER);
        chosen.setOccupation(Job.RULER.getDisplayName());
        chosen.setSocialClass(SocialClass.NOBLE);
        Agent newAgent = world.findAgent(chosen);
        if (newAgent != null) {
            newAgent.setJob(Job.RULER);
        }
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

    /** Reassign an islander's work. Choosing RULER appoints them to the throne. */
    public boolean assignJob(int citizenId, Job job) {
        if (job == null) {
            return false;
        }
        if (job == Job.RULER) {
            return chooseRuler(citizenId);
        }
        Citizen citizen = findCitizen(citizenId);
        if (citizen == null || !citizen.isAlive()) {
            return false;
        }
        citizen.setJob(job);
        citizen.setOccupation(job.getDisplayName());
        if (job == Job.PRIEST) {
            citizen.setSocialClass(SocialClass.CLERGY);
        } else if (job == Job.BUILDER || job == Job.MINER
                || job == Job.TRADER || job == Job.WOODCUTTER) {
            citizen.setSocialClass(SocialClass.ARTISAN);
        } else if (citizen.getSocialClass() == SocialClass.NOBLE) {
            citizen.setSocialClass(SocialClass.LABORER);
        }
        Agent agent = world.findAgent(citizen);
        if (agent != null) {
            agent.setJob(job);
        }
        engine.postEvent(new GameEvent(world.getDay(), EventType.POLITICS, "A new calling",
                citizen.getName() + " is now a " + job.getDisplayName().toLowerCase() + ".", 2));
        return true;
    }

    /** Player-driven construction, paid for from the island's stockpiles. */
    public boolean build(int x, int y, Structure structure) {
        if (structure == null || !structure.isBuildable()) {
            return false;
        }
        Tile tile = world.getMap().get(x, y);
        if (tile == null || !tile.isFreeLand()) {
            return false;
        }
        if (world.getWood() < structure.getWoodCost()
                || world.getStone() < structure.getStoneCost()) {
            return false;
        }
        world.setWood(world.getWood() - structure.getWoodCost());
        world.setStone(world.getStone() - structure.getStoneCost());
        world.getMap().placeStructure(x, y, structure);
        world.incrementStructuresBuilt();
        engine.postEvent(new GameEvent(world.getDay(), EventType.ECONOMY, "Built by your hand",
                structure.getDisplayName() + " rises at " + x + "," + y + ".", 3));
        return true;
    }

    // ------------------------------------------------------------------
    // Deep systems exposed to the front-ends
    // ------------------------------------------------------------------

    public void setLawEnabled(WorldLaw law, boolean enabled) {
        world.setLawEnabled(law, enabled);
        if (law != null) {
            engine.postEvent(new GameEvent(world.getDay(), EventType.LAW,
                    law.name().replace('_', ' '),
                    (enabled ? "Enabled: " : "Disabled: ") + law.getArabicLabel(), 3));
        }
    }

    public void toggleLaw(WorldLaw law) {
        setLawEnabled(law, !world.isLawEnabled(law));
    }

    public void setModPack(ModPack modPack) {
        world.setModPack(modPack);
    }

    public ModPack getModPack() {
        return world.getModPack();
    }

    public List<Kingdom> getKingdoms() {
        return world.getKingdoms();
    }

    public List<Clan> getClans() {
        return world.getClans();
    }

    public List<Culture> getCultures() {
        return world.getCultures();
    }

    public List<War> getWars() {
        return world.getWars();
    }

    /** القوافل بين الممالك — unmodifiable snapshot for the front-end. */
    public List<TradeRoute> getTradeRoutes() {
        return world.getTradeRoutes();
    }

    /** إجمالي القوافل التي وصلت — delivered caravans so far. */
    public int getCaravansCompleted() {
        return world.getCaravansCompleted();
    }

    public List<Technology> getUnlockedTechnologies() {
        List<Technology> result = new ArrayList<>();
        if (world.getCultures().isEmpty()) {
            return result;
        }
        for (String id : world.getCultures().get(0).getTechIds()) {
            try {
                result.add(Technology.valueOf(id));
            } catch (IllegalArgumentException ignored) {
                // Ignore unknown ids from future data packs.
            }
        }
        return result;
    }

    public Technology getNextTechnology() {
        Technology[] all = Technology.values();
        int index = world.getTechsUnlocked();
        return index < all.length ? all[index] : null;
    }

    /** Rush research by feeding the culture; the engine unlocks it on the next day. */
    public boolean researchNextTechnology() {
        Technology next = getNextTechnology();
        if (next == null || world.getCultures().isEmpty()) {
            return false;
        }
        Culture culture = world.getCultures().get(0);
        culture.setKnowledge(Math.max(culture.getKnowledge(), next.getCost()));
        if (world.getGold() < next.getCost() / 4.0) {
            world.setGold(next.getCost() / 4.0);
        }
        return true;
    }

    public void startWar(WarType type, int attackerId, int defenderId) {
        engine.startWar(world.getDay(), type, attackerId, defenderId);
    }

    public void castDisaster(DisasterType type) {
        engine.triggerDisaster(world.getDay(), type);
    }

    public void spawnCreature(AnimalType type) {
        engine.spawnCreature(type);
    }

    /** Place a specific creature exactly where the player's brush touched. */
    public boolean spawnCreatureAt(AnimalType type, int x, int y) {
        if (type == null || !world.getMap().inBounds(x, y)) {
            return false;
        }
        world.addAnimal(new Animal(type, x, y));
        return true;
    }

    /** Create one new settler on a chosen walkable tile. */
    public boolean spawnSettlerAt(int x, int y) {
        Tile tile = world.getMap().get(x, y);
        if (tile == null || !tile.getType().isWalkable()) {
            return false;
        }
        Species species = Species.HUMAN;
        Race race = Race.values()[random.nextInt(Race.values().length)];
        Religion religion = Religion.values()[random.nextInt(Religion.values().length)];
        Ideology ideology = Ideology.values()[random.nextInt(Ideology.values().length)];
        Gender gender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        Job job = random.nextDouble() < 0.6 ? Job.SETTLER : Job.LABORER;
        Citizen citizen = new Citizen(world.nextId(),
                NameGenerator.personName(random, species.getNamePoolKey()),
                gender, race, religion, ideology, 20 * GameConfig.DAYS_PER_YEAR);
        citizen.setSpecies(species);
        citizen.setJob(job);
        citizen.setOccupation(job.getDisplayName());
        citizen.setHealth(70);
        citizen.setHappiness(60);
        world.addCitizen(citizen);
        world.addAgent(new Agent(citizen, x, y, job));
        return true;
    }

    /** Paint terrain directly, like a sandbox brush. */
    public boolean paintTile(int x, int y, TileType type) {
        if (type == null || !world.getMap().inBounds(x, y)) {
            return false;
        }
        return world.getMap().setTerrain(x, y, type);
    }

    public void setRelation(int a, int b, DiplomacyRelation relation) {
        world.setRelation(a, b, relation);
    }

    public void foundClan(int citizenId) {
        Citizen citizen = findCitizen(citizenId);
        if (citizen != null && citizen.isAlive()) {
            engine.foundClan(citizen, world.getDay());
        }
    }

    public void foundKingdom(int citizenId) {
        Citizen citizen = findCitizen(citizenId);
        if (citizen != null && citizen.isAlive()) {
            engine.foundKingdom(citizen, world.getDay());
        }
    }

    /** Build any of the 134 catalogued building types at a chosen tile. */
    public boolean buildBuilding(int x, int y, BuildingType type) {
        if (type == null) {
            return false;
        }
        Tile tile = world.getMap().get(x, y);
        if (tile == null || !tile.isFreeLand()) {
            return false;
        }
        if (world.getWood() < type.getWoodCost() || world.getStone() < type.getStoneCost()
                || world.getGold() < type.getGoldCost()) {
            return false;
        }
        world.setWood(world.getWood() - type.getWoodCost());
        world.setStone(world.getStone() - type.getStoneCost());
        world.setGold(world.getGold() - type.getGoldCost());
        world.getMap().placeStructure(x, y, structureFor(type));
        world.incrementStructuresBuilt();
        engine.postEvent(new GameEvent(world.getDay(), EventType.ECONOMY, "A new building",
                type.getDisplayName() + " rises at " + x + "," + y + ".", 3));
        return true;
    }

    private static Structure structureFor(BuildingType type) {
        switch (type) {
            case HUT:
                return Structure.HUT;
            case HALL:
                return Structure.HALL;
            case FARM:
                return Structure.FARM;
            case GRANARY:
                return Structure.GRANARY;
            case WINDMILL:
                return Structure.WINDMILL;
            case WELL:
                return Structure.WELL;
            case MINE:
                return Structure.MINE;
            case SMITHY:
                return Structure.SMITHY;
            case BARRACKS:
                return Structure.BARRACKS;
            case WATCHTOWER:
                return Structure.TOWER;
            case TEMPLE:
                return Structure.TEMPLE;
            case LIBRARY:
                return Structure.LIBRARY;
            case MARKET:
                return Structure.MARKET;
            case DOCK:
                return Structure.DOCK;
            case ROAD:
                return Structure.ROAD;
            case BRIDGE:
                return Structure.BRIDGE;
            case WAREHOUSE:
                return Structure.WAREHOUSE;
            case GARDEN:
                return Structure.GARDEN;
            case PALACE:
                return Structure.PALACE;
            default:
                switch (type.getCategory()) {
                    case HOUSING:
                        return Structure.HUT;
                    case FOOD:
                        return Structure.FARM;
                    case CRAFT:
                        return Structure.SMITHY;
                    case MILITARY:
                        return Structure.BARRACKS;
                    case CULTURE:
                        return Structure.TEMPLE;
                    case TRADE:
                        return Structure.MARKET;
                    case UTILITY:
                    default:
                        return Structure.WELL;
                }
        }
    }

    public String resourceSummary() {
        int animals = 0;
        for (Animal animal : world.getAnimals()) {
            if (animal.isAlive()) {
                animals++;
            }
        }
        return String.format(Locale.ROOT,
                "Wood %.0f | Stone %.0f | Ore %.0f | Crystal %.0f | Food %.0f | Gold %.0f | Buildings %d | Wildlife %d",
                world.getWood(), world.getStone(), world.getOre(), world.getCrystal(),
                world.getFood(), world.getGold(), world.getMap().countStructures(), animals);
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
