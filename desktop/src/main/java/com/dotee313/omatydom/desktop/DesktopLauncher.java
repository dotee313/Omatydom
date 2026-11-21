package com.dotee313.omatydom.desktop;

import com.dotee313.omatydom.core.BuildingType;
import com.dotee313.omatydom.core.Decree;
import com.dotee313.omatydom.core.DisasterType;
import com.dotee313.omatydom.core.GameEvent;
import com.dotee313.omatydom.core.ModPack;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.Power;
import com.dotee313.omatydom.core.ResourceType;
import com.dotee313.omatydom.core.SimulationListener;
import com.dotee313.omatydom.core.Technology;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.civ.Achievement;
import com.dotee313.omatydom.core.civ.TradeRoute;
import com.dotee313.omatydom.core.civ.WarType;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Job;
import com.dotee313.omatydom.core.model.Ruler;
import com.dotee313.omatydom.core.world.Agent;
import com.dotee313.omatydom.core.world.Animal;
import com.dotee313.omatydom.core.world.AnimalType;
import com.dotee313.omatydom.core.world.MapTemplate;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.WorldLaw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A small command-line front-end for play-testing the complete simulation. */
public final class DesktopLauncher {
    private static OmatydomGame game;

    private DesktopLauncher() {
        // launcher
    }

    public static void main(String[] args) throws IOException {
        long seed = 20261121L;
        MapTemplate template = MapTemplate.ISLANDS;
        ModPack modPack = ModPack.CORE;
        if (args.length > 0) {
            try {
                seed = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Seed must be a number. Using " + seed + " instead.");
            }
        }
        if (args.length > 1) {
            template = parseEnum(MapTemplate.class, args[1], template);
        }
        if (args.length > 2) {
            modPack = parseEnum(ModPack.class, args[2], modPack);
        }

        createGame(seed, template, modPack);
        System.out.println("=== Omatydom v0.8.0 — Living World & User Art | seed " + seed + " ===");
        printHelp();
        printStatus();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("omatydom> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\s+");
            String command = parts[0].toLowerCase(Locale.ROOT);
            if ("quit".equals(command) || "exit".equals(command)) {
                break;
            }
            try {
                handle(command, parts);
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("The chronicle ends here.");
    }

    private static void createGame(long seed, MapTemplate template, ModPack modPack) {
        game = OmatydomGame.create(seed, template, modPack);
        game.getEngine().addListener(new SimulationListener() {
            @Override
            public void onEvent(GameEvent event) {
                System.out.println("   >> " + event);
            }
        });
    }

    private static void handle(String command, String[] parts) {
        switch (command) {
            case "help":
                printHelp();
                break;
            case "status":
                printStatus();
                break;
            case "stats":
                printDeepStats();
                break;
            case "events":
                printEvents(argAsInt(parts, 1, 10));
                break;
            case "step":
            case "run":
                game.advanceDays(argAsInt(parts, 1, 1));
                printStatus();
                break;
            case "pause":
                game.setPaused(true);
                printStatus();
                break;
            case "resume":
                game.setPaused(false);
                printStatus();
                break;
            case "speed":
                game.setSpeed(argAsInt(parts, 1, game.getSpeed()));
                printStatus();
                break;
            case "rulers":
                printRulers();
                break;
            case "choose": {
                int id = argAsInt(parts, 1, -1);
                System.out.println(game.chooseRuler(id)
                        ? "The crown has changed hands." : "No living adult has that id.");
                printStatus();
                break;
            }
            case "assassinate":
                System.out.println(game.assassinateRuler()
                        ? "The deed is done. A succession begins."
                        : "There is no ruler to remove.");
                printStatus();
                break;
            case "control":
                game.takeControl();
                printStatus();
                break;
            case "auto":
                game.releaseControl();
                printStatus();
                break;
            case "power":
                if (parts.length < 2) {
                    printValues(Power.values());
                } else {
                    Power power = parseEnum(Power.class, parts[1], null);
                    boolean done = power != null && game.usePower(power);
                    System.out.println(done ? "The power " + power + " was cast."
                            : "Unknown power, on cooldown, or not enough gold.");
                    printStatus();
                }
                break;
            case "disaster":
                if (parts.length < 2) {
                    printValues(DisasterType.values());
                } else {
                    DisasterType type = parseEnum(DisasterType.class, parts[1], null);
                    if (type == null) {
                        System.out.println("Unknown disaster.");
                    } else {
                        game.castDisaster(type);
                        System.out.println(type.getDisplayName() + " strikes the island.");
                        printStatus();
                    }
                }
                break;
            case "spawn":
                if (parts.length < 2) {
                    printValues(AnimalType.values());
                } else {
                    AnimalType type = parseEnum(AnimalType.class, parts[1], null);
                    if (type == null) {
                        System.out.println("Unknown creature.");
                    } else {
                        game.spawnCreature(type);
                        System.out.println(type.getDisplayName() + " appears.");
                    }
                }
                break;
            case "map":
                printMap();
                break;
            case "agents":
                printAgents();
                break;
            case "resources":
                if (parts.length < 2) {
                    System.out.println(game.resourceSummary());
                    printNamedResources();
                } else {
                    ResourceType type = parseEnum(ResourceType.class, parts[1], null);
                    System.out.println(type == null ? "Unknown resource."
                            : type.getDisplayName() + ": " + game.getWorld().getResource(type));
                }
                break;
            case "animals":
                printAnimals();
                break;
            case "jobs":
                printValues(Job.values());
                break;
            case "job": {
                int id = argAsInt(parts, 1, -1);
                Job job = parts.length > 2 ? parseEnum(Job.class, parts[2], null) : null;
                System.out.println(job != null && game.assignJob(id, job)
                        ? "Work reassigned." : "Give a living id and a valid job.");
                break;
            }
            case "build": {
                int x = argAsInt(parts, 1, -1);
                int y = argAsInt(parts, 2, -1);
                Structure structure = parts.length > 3
                        ? parseEnum(Structure.class, parts[3], null) : null;
                System.out.println(structure != null && game.build(x, y, structure)
                        ? "Construction complete."
                        : "Cannot build there (wrong ground or not enough materials).");
                printStatus();
                break;
            }
            case "building": {
                int x = argAsInt(parts, 1, -1);
                int y = argAsInt(parts, 2, -1);
                BuildingType type = parts.length > 3
                        ? parseEnum(BuildingType.class, parts[3], null) : null;
                System.out.println(type != null && game.buildBuilding(x, y, type)
                        ? type.getDisplayName() + " is finished."
                        : "Give a free land tile and a valid building type with enough materials.");
                printStatus();
                break;
            }
            case "decree":
                if (parts.length < 2) {
                    printValues(Decree.values());
                } else {
                    Decree decree = parseEnum(Decree.class, parts[1], null);
                    System.out.println(game.issueDecree(decree)
                            ? "The decree is proclaimed." : "Unknown decree.");
                    printStatus();
                }
                break;
            case "templates":
                for (MapTemplate value : MapTemplate.values()) {
                    System.out.printf(Locale.ROOT, "%-18s %-16s %s%n",
                            value.name(), value.getArabicDisplayName(), value.getDescription());
                }
                break;
            case "mods":
                for (ModPack value : ModPack.values()) {
                    System.out.printf(Locale.ROOT, "%-14s %-16s birth %.2f work %.2f trait %.2f%n",
                            value.name(), value.getDisplayNameAr(), value.getBirthModifier(),
                            value.getWorkModifier(), value.getTraitChanceModifier());
                }
                break;
            case "new": {
                long seed = parts.length > 1 ? parseLong(parts[1], System.currentTimeMillis()) : System.currentTimeMillis();
                MapTemplate template = parts.length > 2
                        ? parseEnum(MapTemplate.class, parts[2], MapTemplate.ISLANDS) : MapTemplate.ISLANDS;
                ModPack modPack = parts.length > 3
                        ? parseEnum(ModPack.class, parts[3], ModPack.CORE) : ModPack.CORE;
                createGame(seed, template, modPack);
                System.out.println("A new " + template.getDisplayName() + " world begins under " + modPack + ".");
                printStatus();
                break;
            }
            case "laws":
                if (parts.length < 2) {
                    printLaws();
                } else {
                    WorldLaw law = parseEnum(WorldLaw.class, parts[1], null);
                    if (law == null) {
                        System.out.println("Unknown law. Type 'laws' to list them.");
                    } else if (parts.length > 2) {
                        boolean enabled = parts[2].equalsIgnoreCase("on")
                                || parts[2].equalsIgnoreCase("true")
                                || parts[2].equalsIgnoreCase("1");
                        game.setLawEnabled(law, enabled);
                        System.out.println(law.getArabicLabel() + ": " + (enabled ? "on" : "off"));
                    } else {
                        game.toggleLaw(law);
                        System.out.println(law.getArabicLabel() + ": "
                                + (game.getWorld().isLawEnabled(law) ? "on" : "off"));
                    }
                }
                break;
            case "civs":
                printCivilizations();
                break;
            case "trade":
                printTradeRoutes();
                break;
            case "tech":
                if (parts.length > 1 && "research".equalsIgnoreCase(parts[1])) {
                    if (game.researchNextTechnology()) {
                        game.advanceDays(1);
                        System.out.println("Research accelerated. " + progressText());
                    } else {
                        System.out.println("The entire technology tree has been learned.");
                    }
                } else {
                    System.out.println(progressText());
                    Technology next = game.getNextTechnology();
                    System.out.println(next == null ? "Next: none"
                            : "Next: " + next.getDisplayName() + " / " + next.getArabicName()
                                    + " — cost " + next.getCost());
                }
                break;
            case "war":
                if (parts.length < 4) {
                    printValues(WarType.values());
                    System.out.println("Usage: war <TYPE> <ATTACKER_KINGDOM_ID> <DEFENDER_KINGDOM_ID>");
                } else {
                    WarType type = parseEnum(WarType.class, parts[1], null);
                    int attacker = argAsInt(parts, 2, 0);
                    int defender = argAsInt(parts, 3, 0);
                    if (type == null || attacker == defender) {
                        System.out.println("Give a valid war type and two different kingdom ids.");
                    } else {
                        game.startWar(type, attacker, defender);
                        printCivilizations();
                    }
                }
                break;
            case "achievements":
                for (Achievement achievement : Achievement.values()) {
                    System.out.printf(Locale.ROOT, "%-14s %-22s %s%n",
                            game.getWorld().getAchievements().contains(achievement) ? "[X]" : "[ ]",
                            achievement.getDisplayName(), achievement.getDescription());
                }
                break;
            case "biome": {
                int x = argAsInt(parts, 1, -1);
                int y = argAsInt(parts, 2, -1);
                System.out.println(game.getWorld().biomeAt(x, y).getDisplayName());
                break;
            }
            default:
                System.out.println("Unknown command '" + command + "'. Type 'help'.");
        }
    }

    private static int argAsInt(String[] parts, int index, int fallback) {
        if (parts.length <= index) {
            return fallback;
        }
        try {
            return Integer.parseInt(parts[index]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + parts[index] + "' is not a number");
        }
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> type, String value, T fallback) {
        if (value == null) {
            return fallback;
        }
        String needle = value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        for (T candidate : type.getEnumConstants()) {
            String name = candidate.name().replace("_", "");
            if (name.equals(needle) || candidate.name().equalsIgnoreCase(value.trim())) {
                return candidate;
            }
        }
        return fallback;
    }

    private static <T extends Enum<T>> void printValues(T[] values) {
        List<String> names = new ArrayList<>();
        for (T value : values) {
            names.add(value.name());
        }
        System.out.println(String.join(" ", names));
    }

    private static String progressText() {
        int unlocked = game.getWorld().getTechsUnlocked();
        Technology next = game.getNextTechnology();
        return "Technologies " + unlocked + "/" + Technology.count()
                + (next == null ? " — complete" : " — next " + next.getDisplayName());
    }

    private static void printHelp() {
        System.out.println("Core: help | status | stats | events [n] | step [n] | run [n] | pause | resume | speed n");
        System.out.println("People: rulers | choose <id> | assassinate | control | auto | agents | jobs | job <id> <JOB>");
        System.out.println("World: map | resources [TYPE] | animals | biome <x> <y> | build <x> <y> <STRUCTURE>");
        System.out.println("       building <x> <y> <BUILDING_TYPE> | decree [NAME] | power [NAME]");
        System.out.println("Deep: templates | mods | new [seed] [TEMPLATE] [MOD] | laws [KEY [on|off]]");
        System.out.println("      civs | trade | tech [research] | war [TYPE] [A_ID] [B_ID] | disaster [TYPE] | spawn [TYPE]");
        System.out.println("      achievements | quit");
    }

    private static void printStatus() {
        World world = game.getWorld();
        double mood = 0;
        for (Citizen citizen : world.getLivingCitizens()) {
            mood += citizen.getHappiness();
        }
        if (world.getPopulation() > 0) {
            mood /= world.getPopulation();
        }

        Ruler ruler = world.getRuler();
        System.out.println("------------------------------------------------------------");
        System.out.printf(Locale.ROOT,
                "Day %d | Year %d | Population %d | Stability %.1f | Gold %.0f | Food %.0f | Mood %.1f%n",
                world.getDay(), (world.getDay() - 1) / 360 + 1, world.getPopulation(),
                world.getStability(), world.getGold(), world.getFood(), mood);
        System.out.printf(Locale.ROOT, "Template %s | Ruleset %s | Age %s | Season %d | Month %d%n",
                world.getMapTemplate(), game.getModPack(), game.getAge().getDisplayName(),
                OmatydomGame.seasonOf(world.getDay()) + 1, OmatydomGame.monthOf(world.getDay()));
        if (ruler == null) {
            System.out.println("Ruler: NONE — the island waits for a leader.");
        } else {
            System.out.printf(Locale.ROOT,
                    "Ruler: %s (%s) | approval %.1f | %d days reigning | %d decrees%n",
                    ruler.getCitizen().getName(), ruler.getTitle(), ruler.getApproval(),
                    ruler.getReignDays(world.getDay()), ruler.getDecreesIssued());
        }
        System.out.printf(Locale.ROOT,
                "Wood %.0f | Stone %.0f | Ore %.0f | Crystal %.0f | Buildings %d%n",
                world.getWood(), world.getStone(), world.getOre(), world.getCrystal(),
                world.getMap().countStructures());
        System.out.println(progressText() + " | Kingdoms " + world.getKingdoms().size()
                + " | Clans " + world.getClans().size()
                + " | Wars " + world.getWars().size()
                + " | Achievements " + world.getAchievements().size() + "/" + Achievement.values().length);
        System.out.printf(Locale.ROOT, "Clock: %s | speed x%d | throne: %s%n",
                game.isPaused() ? "PAUSED" : "RUNNING", game.getSpeed(),
                game.isPlayerControlled() ? "YOURS" : "AUTONOMOUS");
        System.out.println("------------------------------------------------------------");
    }

    private static void printDeepStats() {
        World world = game.getWorld();
        System.out.printf(Locale.ROOT,
                "Births %d | Deaths %d | Structures %d | Powers %d | Wars %d | Techs %d%n",
                world.getBirths(), world.getDeaths(), world.getStructuresBuilt(),
                world.getPowersUsed(), world.getWarsStarted(), world.getTechsUnlocked());
        System.out.printf(Locale.ROOT, "Clans founded %d | Kingdoms founded %d | Law count %d/%d%n",
                world.getClansFounded(), world.getKingdomsFounded(), world.getLaws().size(),
                WorldLaw.values().length);
    }

    private static void printNamedResources() {
        StringBuilder builder = new StringBuilder("Named stockpile: ");
        for (ResourceType type : ResourceType.values()) {
            builder.append(type.name()).append(' ');
            builder.append(String.format(Locale.ROOT, "%.1f", game.getWorld().getResource(type)));
            builder.append(" | ");
        }
        System.out.println(builder);
    }

    private static void printLaws() {
        for (WorldLaw law : WorldLaw.values()) {
            System.out.printf(Locale.ROOT, "%-8s %-24s %s%n",
                    game.getWorld().isLawEnabled(law) ? "[ON]" : "[OFF]",
                    law.name(), law.getArabicLabel());
        }
    }

    private static void printCivilizations() {
        World world = game.getWorld();
        System.out.println("Kingdoms:");
        for (com.dotee313.omatydom.core.civ.Kingdom kingdom : world.getKingdoms()) {
            System.out.printf(Locale.ROOT, "  #%d %s (%s) capital %d,%d stability %.1f army %d treasury %d%n",
                    kingdom.getId(), kingdom.getName(), kingdom.getSpecies().getDisplayName(),
                    kingdom.getCapitalX(), kingdom.getCapitalY(), kingdom.getStability(),
                    kingdom.getArmy(), kingdom.getTreasury());
        }
        System.out.println("Clans:");
        for (com.dotee313.omatydom.core.civ.Clan clan : world.getClans()) {
            System.out.printf(Locale.ROOT, "  #%d %s influence %d members %d%n",
                    clan.getId(), clan.getName(), clan.getInfluence(), clan.getMemberIds().size());
        }
        System.out.println("Cultures:");
        for (com.dotee313.omatydom.core.civ.Culture culture : world.getCultures()) {
            System.out.printf(Locale.ROOT, "  #%d %s knowledge %.1f techniques %d%n",
                    culture.getId(), culture.getName(), culture.getKnowledge(), culture.getTechIds().size());
        }
        System.out.println("Wars:");
        for (com.dotee313.omatydom.core.civ.War war : world.getWars()) {
            System.out.printf(Locale.ROOT, "  #%d %s kingdom %d vs %d intensity %.2f since day %d%n",
                    war.getId(), war.getType().getDisplayName(), war.getAttackerId(),
                    war.getDefenderId(), war.getIntensity(), war.getStartDay());
        }
    }

    private static void printTradeRoutes() {
        List<TradeRoute> routes = game.getTradeRoutes();
        System.out.println("Delivered caravans: " + game.getCaravansCompleted()
                + " | active routes: " + routes.size());
        for (TradeRoute route : routes) {
            System.out.printf(Locale.ROOT,
                    "  #%d kingdom %d -> %d carrying %s, %.0f%% done, reward %.1f gold%n",
                    route.getId(), route.getFromKingdomId(), route.getToKingdomId(),
                    route.getGood(), route.getProgress() * 100.0, route.getReward());
        }
        if (routes.isEmpty()) {
            System.out.println("  No caravan on the road. Found a second kingdom to open trade.");
        }
    }

    private static void printRulers() {
        List<Citizen> candidates = game.getRulerCandidates();
        if (candidates.isEmpty()) {
            System.out.println("No eligible adults remain.");
            return;
        }
        System.out.printf(Locale.ROOT, "%-5s %-26s %-12s %-12s %-9s %-9s%n",
                "ID", "Name", "Species", "Race", "Loyalty", "Wealth");
        for (Citizen citizen : candidates) {
            System.out.printf(Locale.ROOT, "%-5d %-26s %-12s %-12s %-9.0f %-9.0f%n",
                    citizen.getId(), citizen.getName(), citizen.getSpecies().getDisplayName(),
                    citizen.getRace().getDisplayName(), citizen.getLoyalty(), citizen.getWealth());
        }
    }

    private static void printMap() {
        int width = game.getMap().getWidth();
        int height = game.getMap().getHeight();
        char[][] grid = new char[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = game.getMap().get(x, y).getType().getGlyph();
            }
        }
        for (Animal animal : game.getAnimals()) {
            if (animal.isAlive() && inGrid(animal.getX(), animal.getY(), width, height)) {
                grid[animal.getY()][animal.getX()] = animal.getType().isWater() ? 'f' : 'a';
            }
        }
        for (Agent agent : game.getAgents()) {
            if (agent.getCitizen().isAlive()
                    && inGrid(agent.getX(), agent.getY(), width, height)) {
                grid[agent.getY()][agent.getX()] =
                        Character.toLowerCase(agent.getJob().getMarker());
            }
        }
        System.out.println("  +" + "-".repeat(width) + "+");
        for (int y = 0; y < height; y++) {
            System.out.printf(Locale.ROOT, "%2d|%s|%n", y, new String(grid[y]));
        }
        System.out.println("  +" + "-".repeat(width) + "+");
        System.out.println("~ water  . sand  , grass  T forest  F orchard  ^ mountain  C cave");
        System.out.println("H hut  L hall  P temple  B library  W tower  A barracks  D dock");
        System.out.println("R bridge  M mine  S smithy  F farm  G granary  I windmill  O well");
        System.out.println("K market  = road  N garden  U warehouse  V palace");
        System.out.println("lowercase letters = working islanders, a = animal, f = fish");
    }

    private static boolean inGrid(int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private static void printAgents() {
        System.out.printf(Locale.ROOT, "%-5s %-22s %-14s %-10s %-10s %-9s%n",
                "ID", "Name", "Job", "Class", "Idea", "Position");
        for (Agent agent : game.getAgents()) {
            Citizen citizen = agent.getCitizen();
            if (!citizen.isAlive()) {
                continue;
            }
            System.out.printf(Locale.ROOT, "%-5d %-22s %-14s %-10s %-10s %d,%d%n",
                    citizen.getId(), citizen.getName(), citizen.getJob().getDisplayName(),
                    citizen.getSocialClass().getDisplayName(), citizen.getIdea().getDisplayName(),
                    agent.getX(), agent.getY());
        }
    }

    private static void printAnimals() {
        Map<AnimalType, Integer> counts = new EnumMap<>(AnimalType.class);
        for (Animal animal : game.getAnimals()) {
            if (animal.isAlive()) {
                counts.merge(animal.getType(), 1, Integer::sum);
            }
        }
        for (AnimalType type : AnimalType.values()) {
            System.out.printf(Locale.ROOT, "%-14s %d%n", type.getDisplayName(),
                    counts.getOrDefault(type, 0));
        }
    }

    private static void printEvents(int count) {
        List<GameEvent> events = game.getWorld().getRecentEvents(count);
        if (events.isEmpty()) {
            System.out.println("The chronicle is empty.");
            return;
        }
        for (GameEvent event : events) {
            System.out.println("  " + event);
        }
    }
}
