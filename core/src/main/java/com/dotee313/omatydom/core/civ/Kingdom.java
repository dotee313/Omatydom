package com.dotee313.omatydom.core.civ;

import java.util.LinkedHashSet;
import java.util.Set;

/** A mutable kingdom and the clans that compose it. */
public class Kingdom {
    private int id;
    private String name;
    private Species species;
    private int color;
    private int capitalX;
    private int capitalY;
    private double stability;
    private int army;
    private int treasury;
    private Set<Integer> clanIds;

    public Kingdom(int id, String name, Species species, int color, int capitalX, int capitalY) {
        this.id = id;
        this.name = name;
        this.species = species == null ? Species.HUMAN : species;
        this.color = color;
        this.capitalX = capitalX;
        this.capitalY = capitalY;
        this.stability = 50.0;
        this.army = 0;
        this.treasury = 0;
        this.clanIds = new LinkedHashSet<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species == null ? Species.HUMAN : species;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getCapitalX() {
        return capitalX;
    }

    public void setCapitalX(int capitalX) {
        this.capitalX = capitalX;
    }

    public int getCapitalY() {
        return capitalY;
    }

    public void setCapitalY(int capitalY) {
        this.capitalY = capitalY;
    }

    public double getStability() {
        return stability;
    }

    public void setStability(double stability) {
        this.stability = stability;
    }

    public int getArmy() {
        return army;
    }

    public void setArmy(int army) {
        this.army = army;
    }

    public int getTreasury() {
        return treasury;
    }

    public void setTreasury(int treasury) {
        this.treasury = treasury;
    }

    /** Returns the live clan-id set; mutate it directly if desired. */
    public Set<Integer> getClanIds() {
        return clanIds;
    }

    public void setClanIds(Set<Integer> clanIds) {
        this.clanIds = clanIds == null ? new LinkedHashSet<>() : clanIds;
    }

    public void addClan(int clanId) {
        clanIds.add(clanId);
    }

    public void removeClan(int clanId) {
        clanIds.remove(Integer.valueOf(clanId));
    }
}
