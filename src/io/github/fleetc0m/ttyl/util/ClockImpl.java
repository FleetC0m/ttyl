package io.github.fleetc0m.ttyl.util;

/**
 * Created by Icarus on 8/24/14.
 */
public class ClockImpl implements Clock{

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
