package com.dotee313.omatydom.core.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The island grid: sixteen original world templates, sixteen biome flavours and buildings. */
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

    /** The classic softly-squared island template. */
    public static IslandMap generate(int width, int height, Random random) {
        return generate(width, height, random, MapTemplate.ISLANDS);
    }

    /** Build a deterministic island from one of the sixteen map templates. */
    public static IslandMap generate(int width, int height, Random random, MapTemplate template) {
        MapTemplate chosen = template == null ? MapTemplate.ISLANDS : template;
        int shape = chosen.getShapeCode();
        IslandMap map = new IslandMap(width, height);
        long seed = random.nextLong();
        double cx = width / 2.0;
        double cy = height / 2.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double nx = (x - cx) / (width * 0.46);
                double ny = (y - cy) / (height * 0.44);
                nx += (smoothNoise(x * 0.17, y * 0.17, seed) - 0.5) * 0.22;
                ny += (smoothNoise(x * 0.17 + 41.3, y * 0.17 + 19.7, seed + 17) - 0.5) * 0.22;

                double detail = smoothNoise(x * 0.31 + 53.0, y * 0.31 + 27.0, seed + 7);
                double ridge = smoothNoise(x * 0.55 + 11.0, y * 0.55 + 5.0, seed + 31);
                double wave = (smoothNoise(x * 0.34 + 7.1, y * 0.34 + 3.9, seed + 91) - 0.5) * 0.16;
                double rounded = Math.pow(Math.abs(nx), 3.2) + Math.pow(Math.abs(ny), 3.2);
                double radius = Math.sqrt(nx * nx + ny * ny);
                double angle = Math.atan2(ny, nx);

                boolean land;
                switch (shape) {
                    case 1: // continent
                        land = rounded < 1.32 + wave;
                        break;
                    case 2: // checkerboard
                        land = ((x / 4) + (y / 4)) % 2 == 0;
                        break;
                    case 3: // cheese — a broad landmass punctured by holes
                        land = rounded < 1.05 + wave;
                        if (circle(x, y, width * 0.30, height * 0.30, width * 0.09)) land = false;
                        if (circle(x, y, width * 0.70, height * 0.40, width * 0.07)) land = false;
                        if (circle(x, y, width * 0.55, height * 0.72, width * 0.08)) land = false;
                        break;
                    case 4: // donut
                        land = radius > 0.42 + wave * 0.35 && radius < 0.98 + wave;
                        break;
                    case 5: // anthill — many small mounds
                        land = detail > 0.43 && radius < 1.02 + wave;
                        break;
                    case 6: // volcano
                        land = radius < 1.02 + wave && radius > 0.10;
                        break;
                    case 7: // empty
                        land = false;
                        break;
                    case 8: // box world
                        land = Math.abs(nx) < 0.94 && Math.abs(ny) < 0.94;
                        break;
                    case 9: // pancake
                        land = Math.pow(Math.abs(nx), 4.0) + Math.pow(Math.abs(ny), 2.0) < 0.95 + wave;
                        break;
                    case 10: // toast
                        land = rounded < 1.08 + Math.sin(x * 0.38) * 0.07 + wave;
                        break;
                    case 11: // lasagna — striped land and sea
                        land = Math.sin(y * 0.62 + detail * 2.0) > -0.15 && radius < 1.12;
                        break;
                    case 12: // cubicles
                        land = radius < 1.05 && (x % 7 != 0) && (y % 6 != 0);
                        break;
                    case 13: // bad apple — a bite out of one side
                        land = radius < 0.98 + wave
                                && !circle(x, y, width * 0.86, height * 0.30, width * 0.16);
                        break;
                    case 14: // chaos pearl
                        land = radius < 0.78 + (detail - 0.5) * 0.55 + wave;
                        break;
                    case 15: // dormant volcano
                        land = radius < 1.04 + wave && radius > 0.16 + wave * 0.25;
                        break;
                    case 0: // islands
                    default:
                        land = rounded < 1.0 + wave;
                        break;
                }

                TileType type;
                if (!land) {
                    type = TileType.WATER;
                } else if (shape == 7) {
                    type = TileType.WATER;
                } else if ((shape == 6 || shape == 15) && radius < 0.30) {
                    type = TileType.MOUNTAIN;
                } else if (Math.abs(nx) + Math.abs(ny) > 0.78 + wave) {
                    type = TileType.SAND;
                } else if (shape == 11 && y % 7 == 0) {
                    type = TileType.ORCHARD;
                } else if (shape == 2 && ((x / 4) + (y / 4)) % 3 == 0) {
                    type = TileType.FOREST;
                } else if (detail > 0.69 && radius < 0.80) {
                    type = ridge > 0.72 ? TileType.CAVE : TileType.MOUNTAIN;
                } else if (detail > 0.60) {
                    type = TileType.FOREST;
                } else if (detail > 0.53) {
                    type = TileType.ORCHARD;
                } else {
                    type = TileType.GRASS;
                }

                Biome biome = biomeFor(type, detail, ridge, angle, shape);
                map.tiles[y][x] = new Tile(type, startingResource(type), biome);
            }
        }

        if (shape != 7) {
            ensureMinimum(map, TileType.FOREST, 30, random);
            ensureMinimum(map, TileType.ORCHARD, 14, random);
            ensureMinimum(map, TileType.MOUNTAIN, 16, random);
            ensureMinimum(map, TileType.CAVE, 4, random);
        }
        return map;
    }

    private static boolean circle(int x, int y, double cx, double cy, double radius) {
        double dx = x - cx;
        double dy = y - cy;
        return dx * dx + dy * dy < radius * radius;
    }

    private static Biome biomeFor(TileType type, double detail, double ridge,
                                  double angle, int shape) {
        if (type == TileType.WATER) {
            return Biome.SAND;
        }
        if (shape == 6 || shape == 15) {
            return detail > 0.62 ? Biome.INFERNAL : Biome.WASTELAND;
        }
        if (shape == 3) {
            return Biome.CANDY;
        }
        if (shape == 11) {
            return detail > 0.62 ? Biome.SAVANNA : Biome.GRASS;
        }
        if (type == TileType.MOUNTAIN || type == TileType.CAVE) {
            return ridge > 0.74 ? Biome.CRYSTAL : Biome.PERMAFROST;
        }
        if (type == TileType.FOREST) {
            if (detail > 0.78) return Biome.ENCHANTED;
            if (detail > 0.70) return Biome.MUSHROOM;
            return Biome.JUNGLE;
        }
        if (type == TileType.ORCHARD) {
            return detail > 0.66 ? Biome.CANDY : Biome.SAVANNA;
        }
        if (type == TileType.SAND) {
            return Biome.SAND;
        }
        if (detail < 0.35 && angle > 2.0) {
            return Biome.WASTELAND;
        }
        if (detail > 0.57) {
            return Biome.LEMON;
        }
        return Biome.GRASS;
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

    /**
     * Player brush support: replace one tile's terrain with matching biome and
     * resource. Water also clears any building so nothing floats in the sea.
     */
    public boolean setTerrain(int x, int y, TileType type) {
        Tile tile = get(x, y);
        if (tile == null || type == null) {
            return false;
        }
        tile.setType(type);
        tile.setBiome(Biome.of(type));
        tile.setResource(startingResource(type));
        if (!type.isWalkable()) {
            tile.setStructure(Structure.NONE);
        }
        return true;
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
