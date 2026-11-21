package com.dotee313.omatydom.core.world;

/** One cell of the island: terrain, biome, optional building and remaining resource. */
public final class Tile {
    private TileType type;
    private Biome biome;
    private Structure structure = Structure.NONE;
    private int resource;

    public Tile(TileType type, int resource) {
        this(type, resource, null);
    }

    public Tile(TileType type, int resource, Biome biome) {
        this.type = type == null ? TileType.GRASS : type;
        this.resource = Math.max(0, resource);
        this.biome = biome == null ? Biome.of(this.type) : biome;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type == null ? TileType.GRASS : type;
        if (biome == null) {
            biome = Biome.of(type);
        }
    }

    public Biome getBiome() {
        return biome;
    }

    public void setBiome(Biome biome) {
        this.biome = biome == null ? Biome.of(type) : biome;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure == null ? Structure.NONE : structure;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = Math.max(0, resource);
    }

    public boolean isFreeLand() {
        return type.isWalkable() && structure == Structure.NONE;
    }
}
