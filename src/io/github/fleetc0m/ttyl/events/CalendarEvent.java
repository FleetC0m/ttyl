package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;
import android.provider.CalendarContract;

/**
 * A calendar Event. Should contain a bundle which at least have a field
 * CalendarContract.Instance.DESCRIPTION
 */
public class CalendarEvent extends Event {
    public static final String KEY_BUSY_BOOLEAN = "busy";
    public static final String KEY_CALENDAR_ID_INT = "calendar-id";
    public static final String KEY_CALENDAR_DESCRIPTION_STRING =
            CalendarContract.Events.DESCRIPTION;
    public static final String KEY_CALENDAR_TITLE_STRING =
            CalendarContract.Events.TITLE;

    private final Bundle mBundle;
    public CalendarEvent(Bundle bundle) {
        mBundle = bundle;
    }
    @Override
    public String getEventType() {
        return Event.CALENDAR_ENTRY;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return mBundle;
    }
}
