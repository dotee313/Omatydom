package com.dotee313.omatydom.core.civ;

/** A mutable war record between two kingdoms. */
public class War {
    private int id;
    private WarType type;
    private int attackerId;
    private int defenderId;
    private int startDay;
    private double intensity;

    public War(int id, WarType type, int attackerId, int defenderId, int startDay) {
        this.id = id;
        this.type = type == null ? WarType.CONQUEST : type;
        this.attackerId = attackerId;
        this.defenderId = defenderId;
        this.startDay = startDay;
        this.intensity = 1.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public WarType getType() {
        return type;
    }

    public void setType(WarType type) {
        this.type = type == null ? WarType.CONQUEST : type;
    }

    public int getAttackerId() {
        return attackerId;
    }

    public void setAttackerId(int attackerId) {
        this.attackerId = attackerId;
    }

    public int getDefenderId() {
        return defenderId;
    }

    public void setDefenderId(int defenderId) {
        this.defenderId = defenderId;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }
}
