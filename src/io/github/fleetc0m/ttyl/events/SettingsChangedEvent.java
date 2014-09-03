package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * An event signal the change of a setting item. Additional information should include the changed
 * setting item, the new item value, and optionally the old value.
 */
public class SettingsChangedEvent extends Event {
    private final Bundle mBundle;

    public static final String KEY_SETTING_ITEM_STRING = "setting-item";
    public static final String KEY_SETTING_ITEM_ORIGINAL_STRING = "setting-orig";
    public static final String KEY_SETTING_ITEM_NEW_STRING = "setting-new";

    public SettingsChangedEvent(Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public String getEventType() {
        return Event.SETTINGS_CHANGED;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return mBundle;
    }
}
