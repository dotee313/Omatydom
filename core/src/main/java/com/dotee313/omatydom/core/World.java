package com.dotee313.omatydom.core;

import com.dotee313.omatydom.core.civ.Achievement;
import com.dotee313.omatydom.core.civ.Clan;
import com.dotee313.omatydom.core.civ.Culture;
import com.dotee313.omatydom.core.civ.DiplomacyRelation;
import com.dotee313.omatydom.core.civ.Kingdom;
import com.dotee313.omatydom.core.civ.Species;
import com.dotee313.omatydom.core.civ.War;
import com.dotee313.omatydom.core.civ.WarType;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.ModPack;
import com.dotee313.omatydom.core.ResourceType;
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
import com.dotee313.omatydom.core.world.Biome;
import com.dotee313.omatydom.core.world.IslandMap;
import com.dotee313.omatydom.core.world.MapTemplate;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.WorldLaw;
import com.dotee313.omatydom.core.world.TileType;
import com.dotee313.omatydom.core.world.Weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
    private Weather weather = Weather.CLEAR;
    private int weatherDaysLeft = 3;

    private int births;
    private int deaths;
    private int structuresBuilt;
    private int powersUsed;
    private int warsStarted;
    private int techsUnlocked;
    private int clansFounded;
    private int kingdomsFounded;

    private MapTemplate mapTemplate = MapTemplate.ISLANDS;
    private ModPack modPack = ModPack.CORE;
    private final Map<ResourceType, Double> resources = new EnumMap<>(ResourceType.class);
    private final Set<WorldLaw> laws = EnumSet.noneOf(WorldLaw.class);
    private final Set<Achievement> achievements = EnumSet.noneOf(Achievement.class);
    private final List<Kingdom> kingdoms = new ArrayList<>();
    private final List<Clan> clans = new ArrayList<>();
    private final List<Culture> cultures = new ArrayList<>();
    private final List<War> wars = new ArrayList<>();
    private final Map<Long, DiplomacyRelation> relations = new HashMap<>();
    private final Map<TechEffect, Double> techBonuses = new EnumMap<>(TechEffect.class);

    public World(String name, IslandMap map) {
        this.name = name == null ? "Omatydom" : name;
        this.map = map == null ? new IslandMap(GameConfig.MAP_WIDTH, GameConfig.MAP_HEIGHT) : map;
        for (ResourceType resource : ResourceType.values()) {
            resources.put(resource, resource.getBaseValue() * 2.0);
        }
        for (WorldLaw law : WorldLaw.values()) {
            if (law.isDefaultEnabled()) {
                laws.add(law);
            }
        }
    }

    /** Build a fresh island with terrain, wildlife, resources and a first ruler. */
    public static World create(String name, int population, Random random) {
        return create(name, population, random, MapTemplate.ISLANDS);
    }

    /** Build a fresh island from one of the sixteen map templates. */
    public static World create(String name, int population, Random random, MapTemplate template) {
        MapTemplate chosen = template == null ? MapTemplate.ISLANDS : template;
        IslandMap map = IslandMap.generate(GameConfig.MAP_WIDTH, GameConfig.MAP_HEIGHT, random, chosen);
        World world = new World(name, map);
        world.mapTemplate = chosen;

        for (int i = 0; i < 3; i++) {
            int[] spot = map.randomTileOf(TileType.GRASS, random);
            if (spot != null) {
                map.placeStructure(spot[0], spot[1], Structure.HUT);
            }
        }

        int count = Math.max(1, population);
        for (int i = 0; i < count; i++) {
            Race race = pick(Race.values(), random);
            Species species = pick(Species.values(), random);
            Job job = random.nextDouble() < 0.65 ? race.getPreferredJob() : pick(Job.values(), random);
            if (job == Job.RULER) {
                job = Job.LABORER;
            }
            Gender gender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
            int ageYears = 18 + random.nextInt(50);

            Citizen citizen = new Citizen(
                    world.nextId(),
                    NameGenerator.personName(random, species.getNamePoolKey()),
                    gender,
                    race,
                    pick(Religion.values(), random),
                    pick(Ideology.values(), random),
                    ageYears * GameConfig.DAYS_PER_YEAR
            );
            citizen.setSpecies(species);
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

        world.foundStartingCiv(random);

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

    /** Seed the first culture, kingdom and clan from the founding population. */
    private void foundStartingCiv(Random random) {
        Culture culture = new Culture(1, NameGenerator.cultureName(random));
        culture.addTech(Technology.ANCESTRAL_WISDOM.name());
        cultures.add(culture);
        techsUnlocked = 1;

        Species species = ruler == null ? Species.HUMAN : ruler.getCitizen().getSpecies();
        int cx = width() / 2;
        int cy = height() / 2;
        if (ruler != null) {
            Agent seat = findAgent(ruler.getCitizen());
            if (seat != null) {
                cx = seat.getX();
                cy = seat.getY();
            }
        }
        Kingdom kingdom = new Kingdom(1, NameGenerator.kingdomName(random), species,
                species.getColor(), cx, cy);
        kingdom.setStability(stability);
        kingdom.setArmy(4);
        kingdom.setTreasury(50);
        kingdoms.add(kingdom);
        kingdomsFounded = 1;

        Clan clan = new Clan(1, NameGenerator.clanName(random), 1,
                ruler == null ? 0 : ruler.getCitizen().getId());
        clan.setInfluence(20);
        for (Citizen citizen : citizens) {
            if (citizen.isAdult()) {
                clan.addMember(citizen.getId());
            }
        }
        clans.add(clan);
        clansFounded = 1;
        kingdom.addClan(1);
    }

    private int width() {
        return map.getWidth();
    }

    private int height() {
        return map.getHeight();
    }

    /** Add one animal in a suitable habitat. */
    public static void spawnAnimal(World world, Random random) {
        double roll = random.nextDouble();
        AnimalType type;
        if (roll < 0.04) {
            type = AnimalType.DRAGON;
        } else if (roll < 0.10) {
            AnimalType[] monsters = {AnimalType.WOLF, AnimalType.WORM,
                    AnimalType.GOLEM, AnimalType.ZOMBIE};
            type = monsters[random.nextInt(monsters.length)];
        } else if (roll < 0.38) {
            type = AnimalType.FISH;
        } else if (roll < 0.70) {
            type = AnimalType.DEER;
        } else {
            type = AnimalType.BOAR;
        }
        int[] spot;
        if (type.isWater()) {
            spot = world.map.randomTileOf(TileType.WATER, random);
        } else if (type == AnimalType.DRAGON || type == AnimalType.GOLEM) {
            spot = world.map.randomTileOf(random.nextBoolean() ? TileType.MOUNTAIN : TileType.CAVE, random);
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

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather == null ? Weather.CLEAR : weather;
    }

    public int getWeatherDaysLeft() {
        return weatherDaysLeft;
    }

    public void setWeatherDaysLeft(int days) {
        this.weatherDaysLeft = Math.max(1, days);
    }

    public int getBirths() {
        return births;
    }

    public void incrementBirths() {
        births++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void incrementDeaths() {
        deaths++;
    }

    public int getStructuresBuilt() {
        return structuresBuilt;
    }

    public void incrementStructuresBuilt() {
        structuresBuilt++;
    }

    public int getPowersUsed() {
        return powersUsed;
    }

    public void incrementPowersUsed() {
        powersUsed++;
    }

    public int getWarsStarted() {
        return warsStarted;
    }

    public void incrementWarsStarted() {
        warsStarted++;
    }

    public int getTechsUnlocked() {
        return techsUnlocked;
    }

    public void incrementTechsUnlocked() {
        techsUnlocked++;
    }

    public int getClansFounded() {
        return clansFounded;
    }

    public void incrementClansFounded() {
        clansFounded++;
    }

    public int getKingdomsFounded() {
        return kingdomsFounded;
    }

    public void incrementKingdomsFounded() {
        kingdomsFounded++;
    }

    public MapTemplate getMapTemplate() {
        return mapTemplate;
    }

    public ModPack getModPack() {
        return modPack;
    }

    public void setModPack(ModPack modPack) {
        this.modPack = modPack == null ? ModPack.CORE : modPack;
    }

    /** Read a named stockpile. Core resources mirror the legacy getters. */
    public double getResource(ResourceType type) {
        if (type == null) {
            return 0;
        }
        switch (type) {
            case WOOD:
                return wood;
            case STONE:
            case SAND:
            case CLAY:
                return stone;
            case GOLD:
                return gold;
            case ORE:
            case IRON:
            case COPPER:
            case SILVER:
            case ADAMANTINE:
            case MYTHRIL:
                return ore;
            case CRYSTAL:
            case GEMS:
                return crystal;
            case FISH:
            case MEAT:
            case WHEAT:
            case BREAD:
            case BERRIES:
            case BANANA:
            case COCONUT:
            case LEMONS:
            case PEPPERS:
            case MUSHROOMS:
            case COOKED_MEAT:
            case FLOUR:
            case HERBS:
                return food;
            default:
                Double value = resources.get(type);
                return value == null ? 0 : value;
        }
    }

    /** Add to a named stockpile; also feeds the legacy resource counters. */
    public void addResource(ResourceType type, double amount) {
        if (type == null || amount == 0) {
            return;
        }
        switch (type) {
            case WOOD:
                setWood(wood + amount);
                break;
            case STONE:
                setStone(stone + amount);
                break;
            case GOLD:
                setGold(gold + amount);
                break;
            case ORE:
            case IRON:
            case COPPER:
            case SILVER:
            case ADAMANTINE:
            case MYTHRIL:
                setOre(ore + amount);
                break;
            case CRYSTAL:
            case GEMS:
                setCrystal(crystal + amount);
                break;
            case FISH:
            case MEAT:
            case WHEAT:
            case BREAD:
            case BERRIES:
            case BANANA:
            case COCONUT:
            case LEMONS:
            case PEPPERS:
            case MUSHROOMS:
            case COOKED_MEAT:
            case FLOUR:
            case HERBS:
                setFood(food + amount);
                break;
            default:
                resources.merge(type, amount, Double::sum);
                break;
        }
    }

    public Set<WorldLaw> getLaws() {
        return laws;
    }

    public boolean isLawEnabled(WorldLaw law) {
        return law != null && laws.contains(law);
    }

    public void setLawEnabled(WorldLaw law, boolean enabled) {
        if (law == null) {
            return;
        }
        if (enabled) {
            laws.add(law);
        } else {
            laws.remove(law);
        }
    }

    public Set<Achievement> getAchievements() {
        return achievements;
    }

    public boolean unlockAchievement(Achievement achievement) {
        return achievement != null && achievements.add(achievement);
    }

    public List<Kingdom> getKingdoms() {
        return kingdoms;
    }

    public List<Clan> getClans() {
        return clans;
    }

    public List<Culture> getCultures() {
        return cultures;
    }

    public List<War> getWars() {
        return wars;
    }

    public Map<Long, DiplomacyRelation> getRelations() {
        return relations;
    }

    public DiplomacyRelation getRelation(int a, int b) {
        DiplomacyRelation relation = relations.get(pairKey(a, b));
        return relation == null ? DiplomacyRelation.PEACE : relation;
    }

    public void setRelation(int a, int b, DiplomacyRelation relation) {
        relations.put(pairKey(a, b), relation == null ? DiplomacyRelation.PEACE : relation);
    }

    private static long pairKey(int a, int b) {
        int low = Math.min(a, b);
        int high = Math.max(a, b);
        return ((long) low << 32) | (high & 0xFFFFFFFFL);
    }

    public double getTechBonus(TechEffect effect) {
        Double value = techBonuses.get(effect);
        return value == null ? 0.0 : value;
    }

    public void addTechBonus(TechEffect effect, double amount) {
        if (effect != null && amount > 0) {
            techBonuses.merge(effect, amount, Double::sum);
        }
    }

    public Citizen findCitizen(int id) {
        for (Citizen citizen : citizens) {
            if (citizen.getId() == id) {
                return citizen;
            }
        }
        return null;
    }

    public Biome biomeAt(int x, int y) {
        Tile tile = map.get(x, y);
        return tile == null ? Biome.SAND : tile.getBiome();
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
