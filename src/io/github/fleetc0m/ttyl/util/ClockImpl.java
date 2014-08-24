package io.github.fleetc0m.ttyl.util;

/**
 * Just a regular clock.
 */
public class ClockImpl implements Clock{

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
