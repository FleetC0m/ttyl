package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * An incoming event, for example, when a call incomes, or a meeting on the calendar starts.
 */
public abstract class Event {
    public static final int INCOMING_CALL = 1;
    public static final int AGENDA = 1 << 1;

    public abstract int getEventType();

    public abstract Bundle getAdditionalInfo();
}
