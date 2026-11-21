package com.dotee313.omatydom.desktop;

import com.dotee313.omatydom.core.Decree;
import com.dotee313.omatydom.core.GameEvent;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.SimulationListener;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Ruler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

/** A small command-line front-end for play-testing the simulation. */
public final class DesktopLauncher {
    private static OmatydomGame game;

    private DesktopLauncher() {
        // launcher
    }

    public static void main(String[] args) throws IOException {
        long seed = 20261121L;
        if (args.length > 0) {
            try {
                seed = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Seed must be a number. Using " + seed + " instead.");
            }
        }

        game = OmatydomGame.create(seed);
        game.getEngine().addListener(new SimulationListener() {
            @Override
            public void onEvent(GameEvent event) {
                System.out.println("   >> " + event);
            }
        });

        System.out.println("=== Omatydom v0.1.0 | seed " + seed + " ===");
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

    private static void handle(String command, String[] parts) {
        switch (command) {
            case "help":
                printHelp();
                break;
            case "status":
                printStatus();
                break;
            case "events":
                printEvents(argAsInt(parts, 1, 10));
                break;
            case "step":
            case "run": {
                int days = argAsInt(parts, 1, 1);
                for (int i = 0; i < days; i++) {
                    game.tick();
                }
                printStatus();
                break;
            }
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
                boolean done = game.chooseRuler(id);
                System.out.println(done
                        ? "The crown has changed hands."
                        : "No living adult has that id.");
                printStatus();
                break;
            }
            case "assassinate": {
                boolean done = game.assassinateRuler();
                System.out.println(done
                        ? "The deed is done. A succession begins."
                        : "There is no ruler to remove.");
                printStatus();
                break;
            }
            case "control":
                game.takeControl();
                printStatus();
                break;
            case "auto":
                game.releaseControl();
                printStatus();
                break;
            case "decree":
                if (parts.length < 2) {
                    System.out.print("Available decrees: ");
                    for (Decree decree : Decree.values()) {
                        System.out.print(decree.name() + " ");
                    }
                    System.out.println();
                } else {
                    Decree decree = Decree.valueOf(parts[1].toUpperCase(Locale.ROOT));
                    game.issueDecree(decree);
                    printStatus();
                }
                break;
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

    private static void printHelp() {
        System.out.println("Commands: help | status | events [n] | step [n] | run [n] | pause | resume");
        System.out.println("          speed n | rulers | choose <id> | assassinate | control | auto");
        System.out.println("          decree [NAME] | quit");
    }

    private static void printStatus() {
        World world = game.getWorld();
        List<Citizen> living = world.getLivingCitizens();
        double mood = 0;
        for (Citizen citizen : living) {
            mood += citizen.getHappiness();
        }
        if (!living.isEmpty()) {
            mood /= living.size();
        }

        Ruler ruler = world.getRuler();
        System.out.println("------------------------------------------------------------");
        System.out.printf(Locale.ROOT,
                "Day %d | Population %d | Stability %.1f | Gold %.0f | Food %.0f | Mood %.1f%n",
                world.getDay(), world.getPopulation(), world.getStability(),
                world.getGold(), world.getFood(), mood);
        if (ruler == null) {
            System.out.println("Ruler: NONE — the island waits for a leader.");
        } else {
            System.out.printf(Locale.ROOT,
                    "Ruler: %s (%s) | approval %.1f | %d days reigning | %d decrees%n",
                    ruler.getCitizen().getName(), ruler.getTitle(), ruler.getApproval(),
                    ruler.getReignDays(world.getDay()), ruler.getDecreesIssued());
        }
        System.out.printf(Locale.ROOT, "Clock: %s | speed x%d | throne: %s%n",
                game.isPaused() ? "PAUSED" : "RUNNING",
                game.getSpeed(),
                game.isPlayerControlled() ? "YOURS" : "AUTONOMOUS");
        System.out.println("------------------------------------------------------------");
    }

    private static void printRulers() {
        List<Citizen> candidates = game.getRulerCandidates();
        if (candidates.isEmpty()) {
            System.out.println("No eligible adults remain.");
            return;
        }
        System.out.printf(Locale.ROOT, "%-5s %-26s %-12s %-9s %-9s%n",
                "ID", "Name", "Race", "Loyalty", "Wealth");
        for (Citizen citizen : candidates) {
            System.out.printf(Locale.ROOT, "%-5d %-26s %-12s %-9.0f %-9.0f%n",
                    citizen.getId(), citizen.getName(), citizen.getRace().getDisplayName(),
                    citizen.getLoyalty(), citizen.getWealth());
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
