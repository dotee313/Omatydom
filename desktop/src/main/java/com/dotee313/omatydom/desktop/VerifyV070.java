package com.dotee313.omatydom.desktop;

import com.dotee313.omatydom.core.GameConfig;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.world.Animal;
import com.dotee313.omatydom.core.world.AnimalType;
import com.dotee313.omatydom.core.world.Biome;
import com.dotee313.omatydom.core.world.IslandMap;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;

/**
 * Recreated v0.7.0 sandbox regression for the public core surface.
 *
 * <p>Asserted here: paintTile changes terrain/biome/resources and clears
 * buildings under water; spawnSettlerAt validates bounds and walkability;
 * spawnCreatureAt validates bounds and places the requested animal exactly at
 * the brush coordinates. The current core implementation of spawnCreatureAt
 * does NOT check walkability (it accepts in-bounds water tiles for fish and
 * land animals alike), so no false walkability guarantee is asserted here.</p>
 *
 * <p>Map centering, zoom and pan APIs live in the Android view (IslandView),
 * not in core; they cannot be exercised from a desktop CLI verifier. Renderer
 * and UI changes are compile-checked by the supervisor's Gradle build only.</p>
 */
public final class VerifyV070 {
    private static final long SEED = 7_070_707L;

    private VerifyV070() {
    }

    public static void main(String[] args) {
        try {
            OmatydomGame game = OmatydomGame.create(SEED);
            World world = game.getWorld();
            IslandMap map = game.getMap();
            require(map != null && world.getMap() == map, "game map must be the world map");

            int width = map.getWidth();
            int height = map.getHeight();
            eq(width, GameConfig.MAP_WIDTH, "map width");
            eq(height, GameConfig.MAP_HEIGHT, "map height");
            require(width > 4 && height > 4, "map must have room for sandbox tests");

            checkBoundsGuards(game, map);
            checkPaintTile(game, map);
            checkSettlerBrush(game, world, map);
            checkCreatureBrush(game, world, map);

            int dayBefore = world.getDay();
            game.advanceDays(120);
            require(world.getDay() == dayBefore + 120, "simulation must continue after edits");
            require(map.inBounds(map.getWidth() - 1, map.getHeight() - 1),
                    "map bounds must stay stable");

            System.out.println("VerifyV070 PASS seed=" + SEED
                    + " terrainTypes=" + TileType.values().length
                    + " day=" + world.getDay()
                    + " population=" + world.getPopulation());
        } catch (Throwable failure) {
            System.out.println("VerifyV070 FAIL: " + describe(failure));
            System.exit(1);
        }
    }

    private static void checkBoundsGuards(OmatydomGame game, IslandMap map) {
        int width = map.getWidth();
        int height = map.getHeight();
        require(!game.paintTile(-1, 0, TileType.GRASS), "paintTile accepted x=-1");
        require(!game.paintTile(0, -1, TileType.GRASS), "paintTile accepted y=-1");
        require(!game.paintTile(width, 0, TileType.GRASS), "paintTile accepted x=width");
        require(!game.paintTile(0, height, TileType.GRASS), "paintTile accepted y=height");
        require(!game.paintTile(0, 0, null), "paintTile accepted null terrain");

        require(!game.spawnSettlerAt(-1, 0), "spawnSettlerAt accepted x=-1");
        require(!game.spawnSettlerAt(0, -1), "spawnSettlerAt accepted y=-1");
        require(!game.spawnSettlerAt(width, 0), "spawnSettlerAt accepted x=width");
        require(!game.spawnSettlerAt(0, height), "spawnSettlerAt accepted y=height");

        require(!game.spawnCreatureAt(AnimalType.DEER, -1, 0), "creature x=-1 accepted");
        require(!game.spawnCreatureAt(AnimalType.DEER, 0, -1), "creature y=-1 accepted");
        require(!game.spawnCreatureAt(AnimalType.DEER, width, 0), "creature x=width accepted");
        require(!game.spawnCreatureAt(AnimalType.DEER, 0, height), "creature y=height accepted");
        require(!game.spawnCreatureAt(null, width / 2, height / 2), "null creature accepted");
    }

    private static void checkPaintTile(OmatydomGame game, IslandMap map) {
        int x = map.getWidth() / 2;
        int y = map.getHeight() / 2;

        // Water first: setTerrain clears any building when terrain is not walkable.
        require(game.paintTile(x, y, TileType.WATER), "paint to water failed");
        require(map.typeAt(x, y) == TileType.WATER, "water terrain not applied");
        Tile tile = map.get(x, y);
        require(tile != null && tile.getStructure() == Structure.NONE,
                "water must clear structures");

        require(game.paintTile(x, y, TileType.GRASS), "paint to grass failed");
        require(map.placeStructure(x, y, Structure.HUT), "could not place test hut");
        require(map.get(x, y).getStructure() == Structure.HUT, "test hut missing");
        require(game.paintTile(x, y, TileType.WATER), "second paint to water failed");
        require(map.get(x, y).getStructure() == Structure.NONE,
                "painting water must remove a building");

        int[] expectedResources = {0, 0, 0, 14, 12, 18, 16};
        int index = 0;
        for (TileType type : TileType.values()) {
            require(game.paintTile(x, y, type), "paint to " + type + " failed");
            Tile painted = map.get(x, y);
            require(painted != null && painted.getType() == type,
                    "terrain did not change to " + type);
            require(painted.getBiome() == Biome.of(type),
                    "biome for " + type + " must follow the terrain brush");
            require(map.isWalkable(x, y) == type.isWalkable(),
                    "walkability for " + type + " must follow the terrain brush");
            require(index < expectedResources.length && painted.getResource() == expectedResources[index],
                    "starting resource for " + type + " was " + painted.getResource());
            index++;
        }
        require(index == expectedResources.length, "TileType enum grew; update resource table");

        require(game.paintTile(x, y, TileType.GRASS), "restore grass failed");
        require(map.isWalkable(x, y), "grass must be walkable");
    }

    private static void checkSettlerBrush(OmatydomGame game, World world, IslandMap map) {
        int x = map.getWidth() / 2;
        int y = map.getHeight() / 2;

        require(game.paintTile(x, y, TileType.WATER), "prepare water failed");
        int citizensBefore = world.getCitizens().size();
        require(!game.spawnSettlerAt(x, y), "settler spawned on non-walkable water");
        eq(world.getCitizens().size(), citizensBefore, "citizen count after blocked spawn");

        require(game.paintTile(x, y, TileType.GRASS), "prepare grass failed");
        require(game.spawnSettlerAt(x, y), "settler spawn on walkable grass failed");
        eq(world.getCitizens().size(), citizensBefore + 1, "citizen count after spawn");
        Citizen settler = world.getCitizens().get(world.getCitizens().size() - 1);
        require(settler.isAlive(), "new settler must be alive");
        require(world.findAgent(settler) != null, "new settler needs an agent");
        eq(world.findAgent(settler).getX(), x, "settler agent x");
        eq(world.findAgent(settler).getY(), y, "settler agent y");
    }

    private static void checkCreatureBrush(OmatydomGame game, World world, IslandMap map) {
        int x = map.getWidth() / 2 + 1;
        int y = map.getHeight() / 2;
        require(map.inBounds(x, y), "creature test tile out of bounds");

        require(game.paintTile(x, y, TileType.WATER), "prepare fish water failed");
        int animalsBefore = world.getAnimals().size();
        require(game.spawnCreatureAt(AnimalType.FISH, x, y),
                "in-bounds creature spawn must succeed");
        eq(world.getAnimals().size(), animalsBefore + 1, "animal count after spawn");
        Animal fish = world.getAnimals().get(world.getAnimals().size() - 1);
        eq(fish.getType(), AnimalType.FISH, "spawned creature type");
        eq(fish.getX(), x, "spawned creature x");
        eq(fish.getY(), y, "spawned creature y");
        require(fish.isAlive() && fish.getHealth() > 0, "spawned creature must be alive");

        // Documented v0.7.0 behaviour: bounds are enforced, but the core API
        // does not reject a land animal on water. We assert the real contract
        // (bounds + exact placement) instead of a non-existent guarantee.
        require(game.spawnCreatureAt(AnimalType.DEER, x, y),
                "creature bounds are the only spawn guard in core v0.7.0");

        require(game.paintTile(x, y, TileType.GRASS), "restore grass after creature test");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void eq(int actual, int expected, String label) {
        require(actual == expected, label + ": expected " + expected + " but was " + actual);
    }

    private static void eq(Object actual, Object expected, String label) {
        require(expected.equals(actual), label + ": expected " + expected + " but was " + actual);
    }

    private static String describe(Throwable failure) {
        String message = failure.getMessage();
        return failure.getClass().getSimpleName()
                + (message == null || message.isEmpty() ? "" : ": " + message);
    }
}
