package com.dotee313.omatydom.desktop;

import com.dotee313.omatydom.core.BuildingType;
import com.dotee313.omatydom.core.GameConfig;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.world.Biome;
import com.dotee313.omatydom.core.world.IslandMap;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;

import java.util.Locale;

/**
 * v0.8.0 sea / building / land-brush regression, in the style of VerifyV080.
 *
 * <p>Assertions: the default 40x24 island keeps 35%-80% water for seeds 1..5,
 * averages at least the conservative 0.40 baseline and always leaves more than
 * 20% land; {@code buildBuilding} succeeds on free land with exact resource
 * deductions and a +1 structure counter, and refuses water, out-of-bounds and
 * insufficient resources (including a gold-shortage case); painting a coastal
 * water tile to sand converts only that tile, matches the sand biome and leaves
 * deep water untouched; the 270/360/90/720 calendar constants and short runs
 * still hold. Only public core APIs are used - no Android classes.</p>
 *
 * <p>The direct compile-time reference to Worker 4's {@code IslandMap.waterRatio()}
 * is intentional (as in VerifyV080): compilation is expected to fail until the
 * supervisor merges the Worker 4 map API.</p>
 */
public final class VerifyV081 {
    private static final long[] SEEDS = {1L, 2L, 3L, 4L, 5L};
    private static final double MIN_WATER_RATIO = 0.35;
    private static final double MAX_WATER_RATIO = 0.80;
    private static final double BASELINE_WATER_RATIO = 0.40;
    private static final double MIN_LAND_RATIO = 0.20;
    private static final double EPS = 1e-6;
    private static final int SHORT_RUN_DAYS = 12;

    private VerifyV081() {
    }

    public static void main(String[] args) {
        try {
            eq(GameConfig.MAP_WIDTH, 40, "default map width");
            eq(GameConfig.MAP_HEIGHT, 24, "default map height");
            double averageWater = checkSea();
            checkBuilding();
            checkLandExpansion();
            checkCalendarAndShortRun();
            System.out.printf(Locale.ROOT,
                    "VerifyV081 PASS seeds=%d avgWaterRatio=%.4f minWater=%.2f maxWater=%.2f baseline=%.2f%n",
                    SEEDS.length, averageWater, MIN_WATER_RATIO, MAX_WATER_RATIO,
                    BASELINE_WATER_RATIO);
        } catch (Throwable failure) {
            System.out.println("VerifyV081 FAIL: " + describe(failure));
            System.exit(1);
        }
    }

    /** (a) Sea share of every default island, cross-checked against tile counts. */
    private static double checkSea() {
        double sum = 0.0;
        for (long seed : SEEDS) {
            IslandMap map = OmatydomGame.create(seed).getMap();
            eq(map.getWidth(), GameConfig.MAP_WIDTH, "seed " + seed + ": map width");
            eq(map.getHeight(), GameConfig.MAP_HEIGHT, "seed " + seed + ": map height");
            int total = map.getWidth() * map.getHeight();
            int waterTiles = map.count(TileType.WATER);
            double ratio = map.waterRatio();
            require(Double.isFinite(ratio), "seed " + seed + ": non-finite waterRatio " + ratio);
            require(ratio >= MIN_WATER_RATIO && ratio <= MAX_WATER_RATIO,
                    "seed " + seed + ": waterRatio " + ratio + " outside ["
                            + MIN_WATER_RATIO + ", " + MAX_WATER_RATIO + "]");
            close(ratio, (double) waterTiles / total,
                    "seed " + seed + ": waterRatio disagrees with WATER tile count");
            double landRatio = (double) (total - waterTiles) / total;
            require(landRatio > MIN_LAND_RATIO,
                    "seed " + seed + ": remaining land only " + landRatio);
            require(total - waterTiles > 0, "seed " + seed + ": no land tile remains");
            sum += ratio;
            System.out.printf(Locale.ROOT,
                    "  seed=%d water=%d/%d waterRatio=%.4f landRatio=%.4f%n",
                    seed, waterTiles, total, ratio, landRatio);
        }
        double average = sum / SEEDS.length;
        require(average >= BASELINE_WATER_RATIO,
                "average waterRatio " + average + " below baseline " + BASELINE_WATER_RATIO);
        return average;
    }

    /** (b) Successful construction, exact costs, and all three refusal paths. */
    private static void checkBuilding() {
        OmatydomGame game = OmatydomGame.create(SEEDS[0]);
        World world = game.getWorld();
        IslandMap map = game.getMap();

        BuildingType type = BuildingType.REED_SHACK;
        int[] free = findFreeLand(map);
        require(free != null, "no free land tile to build on");
        grantMaterials(world);
        double wood = world.getWood();
        double stone = world.getStone();
        double gold = world.getGold();
        int built = world.getStructuresBuilt();
        require(game.buildBuilding(free[0], free[1], type),
                "buildBuilding failed on free land with ample materials");
        close(world.getWood(), wood - type.getWoodCost(), "wood deduction");
        close(world.getStone(), stone - type.getStoneCost(), "stone deduction");
        close(world.getGold(), gold - type.getGoldCost(), "gold deduction");
        eq(world.getStructuresBuilt(), built + 1, "structuresBuilt after success");
        require(map.get(free[0], free[1]).getStructure() != Structure.NONE,
                "no structure was placed on the target tile");

        int[] water = findTile(map, TileType.WATER);
        require(water != null, "no water tile to test refused construction");
        grantMaterials(world);
        Snapshot before = snapshot(world, map);
        require(!game.buildBuilding(water[0], water[1], type), "building on water was accepted");
        assertUnchanged(world, map, before, "water build");

        grantMaterials(world);
        before = snapshot(world, map);
        require(!game.buildBuilding(-1, 0, type)
                        && !game.buildBuilding(map.getWidth(), 0, type)
                        && !game.buildBuilding(0, -1, type)
                        && !game.buildBuilding(0, map.getHeight(), type),
                "out-of-bounds construction was accepted");
        assertUnchanged(world, map, before, "out-of-bounds build");

        grantMaterials(world);
        world.setWood(type.getWoodCost() - 1);
        before = snapshot(world, map);
        int[] other = findFreeLand(map);
        require(other != null, "no second free land tile for the shortage test");
        require(!game.buildBuilding(other[0], other[1], type),
                "building with insufficient wood was accepted");
        assertUnchanged(world, map, before, "wood shortage build");

        BuildingType goldType = BuildingType.HALL;
        require(goldType.getGoldCost() > 0, "HALL must have a positive gold cost");
        grantMaterials(world);
        world.setGold(goldType.getGoldCost() - 1);
        before = snapshot(world, map);
        require(!game.buildBuilding(other[0], other[1], goldType),
                "building with insufficient gold was accepted");
        assertUnchanged(world, map, before, "gold shortage build");
    }

    /** (c) Coastal water becomes sand; distant water and buildings are unaffected. */
    private static void checkLandExpansion() {
        OmatydomGame game = OmatydomGame.create(SEEDS[0]);
        World world = game.getWorld();
        IslandMap map = game.getMap();
        int[] coastalWater = findCoastalWater(map);
        require(coastalWater != null, "no water tile next to land");
        int[] deepWater = findDeepWater(map);
        require(deepWater != null, "no deep water tile");
        require(deepWater[0] != coastalWater[0] || deepWater[1] != coastalWater[1],
                "deep water and coastal water must be different tiles");

        int total = map.getWidth() * map.getHeight();
        int waterBefore = map.count(TileType.WATER);
        int sandBefore = map.count(TileType.SAND);
        double ratioBefore = map.waterRatio();
        Snapshot before = snapshot(world, map);
        require(game.paintTile(coastalWater[0], coastalWater[1], TileType.SAND),
                "paintTile(SAND) failed on coastal water");
        eq(map.typeAt(coastalWater[0], coastalWater[1]), TileType.SAND, "painted terrain");
        eq(map.get(coastalWater[0], coastalWater[1]).getBiome(), Biome.SAND,
                "painted tile biome");
        eq(map.count(TileType.WATER), waterBefore - 1, "water tile count after painting");
        eq(map.count(TileType.SAND), sandBefore + 1, "sand tile count after painting");
        close(map.waterRatio(), ratioBefore - 1.0 / total, "waterRatio after painting");
        eq(map.typeAt(deepWater[0], deepWater[1]), TileType.WATER,
                "distant water changed by the paint brush");
        assertUnchanged(world, map, before, "paint tile");

        int day = world.getDay();
        game.advanceDays(SHORT_RUN_DAYS);
        eq(world.getDay(), day + SHORT_RUN_DAYS, "day counter after painting run");
        eq(map.typeAt(coastalWater[0], coastalWater[1]), TileType.SAND,
                "painted land reverted during simulation");
        eq(map.typeAt(deepWater[0], deepWater[1]), TileType.WATER,
                "distant water converted itself during simulation");
        eq(map.count(TileType.WATER), waterBefore - 1, "water count drifted during simulation");
    }

    /** (d) Calendar constants and short, exception-free advances. */
    private static void checkCalendarAndShortRun() {
        eq(GameConfig.GESTATION_DAYS, 270, "gestation days");
        eq(GameConfig.MIN_BIRTH_SPACING_DAYS, 360, "post-birth spacing days");
        eq(GameConfig.DAYS_PER_SEASON, 90, "days per season");
        eq(GameConfig.DAYS_PER_AGE, 720, "days per age");
        eq(GameConfig.DAYS_PER_YEAR, 360, "days per year");

        OmatydomGame daily = OmatydomGame.create(77L);
        int start = daily.getWorld().getDay();
        for (int i = 0; i < SHORT_RUN_DAYS; i++) {
            daily.advanceDays(1);
        }
        eq(daily.getWorld().getDay(), start + SHORT_RUN_DAYS, "daily advanceDays counter");
        require(Double.isFinite(daily.getWorld().getGold())
                        && Double.isFinite(daily.getWorld().getWood())
                        && Double.isFinite(daily.getWorld().getFood()),
                "resources became non-finite during the daily run");

        OmatydomGame bulk = OmatydomGame.create(78L);
        int bulkStart = bulk.getWorld().getDay();
        bulk.advanceDays(3);
        eq(bulk.getWorld().getDay(), bulkStart + 3, "bulk advanceDays counter");
    }

    private static void grantMaterials(World world) {
        world.setWood(500.0);
        world.setStone(500.0);
        world.setGold(500.0);
    }

    private static Snapshot snapshot(World world, IslandMap map) {
        return new Snapshot(world.getWood(), world.getStone(), world.getGold(),
                world.getStructuresBuilt(), map.countStructures());
    }

    private static void assertUnchanged(World world, IslandMap map, Snapshot before,
                                        String label) {
        close(world.getWood(), before.wood, label + " wood");
        close(world.getStone(), before.stone, label + " stone");
        close(world.getGold(), before.gold, label + " gold");
        eq(world.getStructuresBuilt(), before.built, label + " structuresBuilt");
        eq(map.countStructures(), before.placed, label + " placed structures");
    }

    private static int[] findTile(IslandMap map, TileType type) {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.typeAt(x, y) == type) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static int[] findFreeLand(IslandMap map) {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = map.get(x, y);
                if (tile != null && tile.isFreeLand()) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static int[] findCoastalWater(IslandMap map) {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.typeAt(x, y) == TileType.WATER && hasLandNeighbour(map, x, y)) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static int[] findDeepWater(IslandMap map) {
        int[] best = null;
        int bestDistance = -1;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.typeAt(x, y) != TileType.WATER) {
                    continue;
                }
                int distance = minLandDistance(map, x, y);
                if (distance > bestDistance) {
                    bestDistance = distance;
                    best = new int[]{x, y};
                }
            }
        }
        return best;
    }

    private static boolean hasLandNeighbour(IslandMap map, int x, int y) {
        return isLand(map, x - 1, y) || isLand(map, x + 1, y)
                || isLand(map, x, y - 1) || isLand(map, x, y + 1);
    }

    private static boolean isLand(IslandMap map, int x, int y) {
        Tile tile = map.get(x, y);
        return tile != null && tile.getType().isWalkable();
    }

    private static int minLandDistance(IslandMap map, int x, int y) {
        int best = Integer.MAX_VALUE;
        for (int ty = 0; ty < map.getHeight(); ty++) {
            for (int tx = 0; tx < map.getWidth(); tx++) {
                if (isLand(map, tx, ty)) {
                    best = Math.min(best, Math.abs(tx - x) + Math.abs(ty - y));
                }
            }
        }
        return best;
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

    private static void close(double actual, double expected, String label) {
        require(Math.abs(actual - expected) <= EPS,
                label + ": expected " + expected + " but was " + actual);
    }

    private static String describe(Throwable failure) {
        String message = failure.getMessage();
        return failure.getClass().getSimpleName()
                + (message == null || message.isEmpty() ? "" : ": " + message);
    }

    private static final class Snapshot {
        final double wood;
        final double stone;
        final double gold;
        final int built;
        final int placed;

        Snapshot(double wood, double stone, double gold, int built, int placed) {
            this.wood = wood;
            this.stone = stone;
            this.gold = gold;
            this.built = built;
            this.placed = placed;
        }
    }
}
