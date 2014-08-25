package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * A calendar Event. Should contain a bundle which at least have a field
 * CalendarContract.Instance.DESCRIPTION
 */
public class CalendarEvent extends Event {
    public static final String KEY_BUSY_BOOLEAN = "busy";

    private final Bundle mBundle;
    public CalendarEvent(Bundle bundle) {
        mBundle = bundle;
    }
    @Override
    public int getEventType() {
        return Event.AGENDA;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return mBundle;
    }
}
