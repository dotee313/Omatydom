package com.dotee313.omatydom.core;

/** One entry in the island chronicle. */
public final class GameEvent {
    private final int day;
    private final EventType type;
    private final String title;
    private final String description;
    private final int severity;

    public GameEvent(int day, EventType type, String title, String description, int severity) {
        this.day = day;
        this.type = type;
        this.title = title == null ? "" : title;
        this.description = description == null ? "" : description;
        this.severity = Math.max(1, Math.min(5, severity));
    }

    public int getDay() {
        return day;
    }

    public EventType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    /** 1 = minor, 5 = world-shaking. */
    public int getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return "[Day " + day + "] " + type.getDisplayName() + ": " + title + " — " + description;
    }
}
