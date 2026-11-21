package com.dotee313.omatydom.core;

/** Optional observer of the simulation, used by front-ends for live chronicles. */
public interface SimulationListener {

    void onEvent(GameEvent event);

    default void onDayAdvanced(int day) {
        // optional
    }
}
