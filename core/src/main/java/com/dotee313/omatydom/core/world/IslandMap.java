package com.dotee313.omatydom.core.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The island grid: water, sand, forests, orchards, mountains and caves. */
public final class IslandMap {
    private final int width;
    private final int height;
    private final Tile[][] tiles;

    public IslandMap(int width, int height) {
        this.width = Math.max(8, width);
        this.height = Math.max(8, height);
        this.tiles = new Tile[this.height][this.width];
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                tiles[y][x] = new Tile(TileType.GRASS, 0);
            }
        }
    }

    /**
     * Build a deterministic island. The outline is a rounded square distorted by
     * smooth noise: less circular than a disc, less rigid than a rectangle.
     */
    public static IslandMap generate(int width, int height, Random random) {
        IslandMap map = new IslandMap(width, height);
        long seed = random.nextLong();
        double cx = width / 2.0;
        double cy = height / 2.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double nx = (x - cx) / (width * 0.46);
                double ny = (y - cy) / (height * 0.44);

                // Warp the rounded-square coordinates so coves and headlands appear.
                nx += (smoothNoise(x * 0.17, y * 0.17, seed) - 0.5) * 0.26;
                ny += (smoothNoise(x * 0.17 + 41.3, y * 0.17 + 19.7, seed + 17) - 0.5) * 0.26;

                // Power 3.2 gives a square with soft corners, not a circle.
                double roundedSquare = Math.pow(Math.abs(nx), 3.2) + Math.pow(Math.abs(ny), 3.2);
                double edgeNoise = (smoothNoise(x * 0.34 + 7.1, y * 0.34 + 3.9, seed + 91) - 0.5) * 0.16;
                double edge = 1.0 + edgeNoise;

                double detail = smoothNoise(x * 0.31 + 53.0, y * 0.31 + 27.0, seed + 7);
                double ridge = smoothNoise(x * 0.55 + 11.0, y * 0.55 + 5.0, seed + 31);

                TileType type;
                if (roundedSquare > edge) {
                    type = TileType.WATER;
                } else if (roundedSquare > edge - 0.22) {
                    type = TileType.SAND;
                } else if (detail > 0.69 && roundedSquare < edge - 0.30) {
                    type = TileType.MOUNTAIN;
                    if (ridge > 0.72) {
                        type = TileType.CAVE;
                    }
                } else if (detail > 0.60) {
                    type = TileType.FOREST;
                } else if (detail > 0.53) {
                    type = TileType.ORCHARD;
                } else {
                    type = TileType.GRASS;
                }

                map.tiles[y][x] = new Tile(type, startingResource(type));
            }
        }

        ensureMinimum(map, TileType.FOREST, 30, random);
        ensureMinimum(map, TileType.ORCHARD, 14, random);
        ensureMinimum(map, TileType.MOUNTAIN, 16, random);
        ensureMinimum(map, TileType.CAVE, 4, random);
        return map;
    }

    /** Smooth value noise: coherent patches instead of single-pixel speckles. */
    private static double smoothNoise(double x, double y, long seed) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        double tx = x - x0;
        double ty = y - y0;
        tx = tx * tx * (3.0 - 2.0 * tx);
        ty = ty * ty * (3.0 - 2.0 * ty);

        double top = hash(x0, y0, seed) * (1.0 - tx) + hash(x0 + 1, y0, seed) * tx;
        double bottom = hash(x0, y0 + 1, seed) * (1.0 - tx)
                + hash(x0 + 1, y0 + 1, seed) * tx;
        return top * (1.0 - ty) + bottom * ty;
    }

    private static double hash(int x, int y, long seed) {
        long value = seed + x * 374761393L + y * 668265263L;
        value = (value ^ (value >>> 13)) * 1274126177L;
        value ^= value >>> 16;
        return (value & 0xFFFFFFFFL) / 4294967295.0;
    }

    private static int startingResource(TileType type) {
        switch (type) {
            case FOREST:
                return 14;
            case ORCHARD:
                return 12;
            case MOUNTAIN:
                return 18;
            case CAVE:
                return 16;
            default:
                return 0;
        }
    }

    private static void ensureMinimum(IslandMap map, TileType type, int minimum, Random random) {
        int have = map.count(type);
        List<int[]> candidates = new ArrayList<>();
        for (int y = 1; y < map.height - 1; y++) {
            for (int x = 1; x < map.width - 1; x++) {
                if (map.tiles[y][x].getType() == TileType.GRASS) {
                    candidates.add(new int[]{x, y});
                }
            }
        }
        int needed = minimum - have;
        while (needed > 0 && !candidates.isEmpty()) {
            int[] spot = candidates.remove(random.nextInt(candidates.size()));
            Tile tile = map.tiles[spot[1]][spot[0]];
            tile.setType(type);
            tile.setResource(startingResource(type));
            needed--;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public Tile get(int x, int y) {
        if (!inBounds(x, y)) {
            return null;
        }
        return tiles[y][x];
    }

    public TileType typeAt(int x, int y) {
        Tile tile = get(x, y);
        return tile == null ? TileType.WATER : tile.getType();
    }

    public boolean isWalkable(int x, int y) {
        Tile tile = get(x, y);
        return tile != null && tile.getType().isWalkable();
    }

    public int count(TileType type) {
        int total = 0;
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                if (tile.getType() == type) {
                    total++;
                }
            }
        }
        return total;
    }

    public int countStructures() {
        int total = 0;
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                if (tile.getStructure() != Structure.NONE) {
                    total++;
                }
            }
        }
        return total;
    }

    /** Finds the closest tile of the given type, or null if none exists. */
    public int[] findNearest(int fromX, int fromY, TileType type) {
        int bestDistance = Integer.MAX_VALUE;
        int[] best = null;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x].getType() != type) {
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

    /** Finds the closest land tile next to water, used by fishers. */
    public int[] findNearestShore(int fromX, int fromY) {
        int bestDistance = Integer.MAX_VALUE;
        int[] best = null;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile tile = tiles[y][x];
                if (!tile.getType().isWalkable()) {
                    continue;
                }
                if (!hasWaterNeighbour(x, y)) {
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

    public boolean hasWaterNeighbour(int x, int y) {
        return typeAt(x - 1, y) == TileType.WATER
                || typeAt(x + 1, y) == TileType.WATER
                || typeAt(x, y - 1) == TileType.WATER
                || typeAt(x, y + 1) == TileType.WATER;
    }

    public int[] randomWalkable(Random random) {
        List<int[]> spots = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x].getType().isWalkable()) {
                    spots.add(new int[]{x, y});
                }
            }
        }
        if (spots.isEmpty()) {
            return new int[]{width / 2, height / 2};
        }
        return spots.get(random.nextInt(spots.size()));
    }

    public int[] randomTileOf(TileType type, Random random) {
        List<int[]> spots = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x].getType() == type) {
                    spots.add(new int[]{x, y});
                }
            }
        }
        if (spots.isEmpty()) {
            return null;
        }
        return spots.get(random.nextInt(spots.size()));
    }

    public boolean placeStructure(int x, int y, Structure structure) {
        Tile tile = get(x, y);
        if (tile == null || !tile.isFreeLand() || structure == Structure.NONE) {
            return false;
        }
        tile.setStructure(structure);
        return true;
    }

    /** Slowly restores harvestable tiles so the island never runs dry forever. */
    public void regenerate(Random random) {
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                TileType type = tile.getType();
                int cap;
                if (type == TileType.FOREST) {
                    cap = 40;
                } else if (type == TileType.ORCHARD) {
                    cap = 32;
                } else if (type == TileType.MOUNTAIN || type == TileType.CAVE) {
                    cap = 40;
                } else {
                    continue;
                }
                if (tile.getResource() < cap && random.nextDouble() < 0.5) {
                    tile.setResource(tile.getResource() + 1);
                }
            }
        }
    }
}
