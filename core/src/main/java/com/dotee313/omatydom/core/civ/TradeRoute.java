package com.dotee313.omatydom.core.civ;

/**
 * A mutable trade-caravan record between two kingdoms.
 * progress stays in 0..1; a caravan becomes inactive on the day it arrives.
 */
public final class TradeRoute {
    private final int id;
    private final int fromKingdomId;
    private final int toKingdomId;
    private final String good;
    private final double reward;
    private final int durationDays;
    private double progress;
    private boolean active;

    public TradeRoute(int id, int fromKingdomId, int toKingdomId,
                      String good, double reward, int durationDays) {
        this.id = id;
        this.fromKingdomId = fromKingdomId;
        this.toKingdomId = toKingdomId;
        this.good = good == null || good.isEmpty() ? "Goods" : good;
        this.reward = Math.max(0.0, reward);
        this.durationDays = Math.max(1, durationDays);
        this.progress = 0.0;
        this.active = true;
    }

    public int getId() {
        return id;
    }

    public int getFromKingdomId() {
        return fromKingdomId;
    }

    public int getToKingdomId() {
        return toKingdomId;
    }

    public String getGood() {
        return good;
    }

    /** 0..1 fraction of the journey already travelled. */
    public double getProgress() {
        return progress;
    }

    /** Gold value paid to the island and both kingdoms on arrival. */
    public double getReward() {
        return reward;
    }

    public boolean isActive() {
        return active;
    }

    /** Advance one game day; returns true on the day the caravan delivers. */
    public boolean advanceOneDay() {
        if (!active) {
            return false;
        }
        progress = Math.min(1.0, progress + 1.0 / durationDays);
        if (progress >= 1.0) {
            active = false;
            return true;
        }
        return false;
    }

    /** End the journey early when an involved kingdom no longer exists. */
    public void cancel() {
        active = false;
    }
}
