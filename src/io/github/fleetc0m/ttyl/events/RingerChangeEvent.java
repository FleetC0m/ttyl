package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * Created by Icarus on 8/26/14.
 */
public class RingerChangeEvent extends Event{
    public static String KEY_NEW_RINGER_MODE_INT = "new_ringer_mode";
    public static String KEY_ORIG_RINGER_MODE_INT = "orig_ringer_mode";

    private final Bundle mBundle;

    public RingerChangeEvent(int oldMode, int newMode) {
        mBundle = new Bundle();
        mBundle.putInt(KEY_ORIG_RINGER_MODE_INT, oldMode);
        mBundle.putInt(KEY_NEW_RINGER_MODE_INT, newMode);
    }

    @Override
    public String getEventType() {
        return Event.RINGER_CHANGE;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return mBundle;
    }
}
