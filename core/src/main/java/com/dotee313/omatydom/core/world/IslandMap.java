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

    /** Build a deterministic island shape around the centre of the grid. */
    public static IslandMap generate(int width, int height, Random random) {
        IslandMap map = new IslandMap(width, height);
        long seed = random.nextLong();
        double cx = width / 2.0;
        double cy = height / 2.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double nx = (x - cx) / (width * 0.48);
                double ny = (y - cy) / (height * 0.46);
                double radius = nx * nx + ny * ny;
                double noise = noise(x, y, seed);

                TileType type;
                if (radius > 1.02) {
                    type = TileType.WATER;
                } else if (radius > 0.82) {
                    type = TileType.SAND;
                } else if (noise > 0.80 && radius < 0.70) {
                    type = TileType.MOUNTAIN;
                    if (noise(x + 31, y + 17, seed) > 0.84) {
                        type = TileType.CAVE;
                    }
                } else if (noise > 0.66) {
                    type = TileType.FOREST;
                } else if (noise > 0.58) {
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

    private static double noise(int x, int y, long seed) {
        double value = Math.sin(x * 12.9898 + y * 78.233 + seed * 0.00001) * 43758.5453;
        return value - Math.floor(value);
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
