package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * Controls the ringer.
 */
public class RingerControlEvent extends Event {
    public static final String KEY_RINGER_MODE_INT = "key-ringer-mode-int";
    public static final String KEY_RINGER_MODE_REVERT_BOOLEAN = "key-ringer-mode-revert-boolean";

    private final Bundle mBundle;

    public RingerControlEvent(Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public String getEventType() {
        return Event.RINGER_CONTROL;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return mBundle;
    }
}
