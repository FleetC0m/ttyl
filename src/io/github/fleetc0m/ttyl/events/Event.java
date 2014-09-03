package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * An incoming event, for example, when a call incomes, or a meeting on the calendar starts.
 */
public abstract class Event {
    public static final String CALENDAR_ENTRY = "calendar";
    public static final String DRIVING = "driving";
    public static final String RINGER_CHANGE = "ringer-changed";
    public static final String QUIT = "quit";
    public static final String SETTINGS_CHANGED = "settings-changed";
    public static final String RINGER_CONTROL = "ringer-control";
    public static final String INCOMING_CALL = "incoming-call";

    public abstract String getEventType();

    public abstract Bundle getAdditionalInfo();
}
