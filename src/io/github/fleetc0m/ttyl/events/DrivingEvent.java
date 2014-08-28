package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * Event signify whether user is driving.
 */
public class DrivingEvent extends Event {
    public static final String KEY_DRIVING_BOOLEAN = "driving";
    public static final String KEY_DRIVING_SPEED_DOUBLE = "driving-speed";

    private final Bundle mBundle;

    public DrivingEvent(Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public String getEventType() {
        return Event.DRIVING;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return mBundle;
    }
}
