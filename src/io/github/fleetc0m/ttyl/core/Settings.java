package io.github.fleetc0m.ttyl.core;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.common.annotations.VisibleForTesting;

/**
 * A utility class bookkeeps all the settings in a SharedPerference based presistent storage.
 * The user of Settings maintain their own storage management semantic, though this class might
 * keep a log of the change of settings item.
 */
public class Settings {
    @VisibleForTesting
    public static final String SHARED_PREF_NAME = "io.github.fleetc0m.ttyl.SharedPref";

    private final Context mContext;
    private final SharedPreferences mSharedPreferences;

    private static Settings mSettings;

    @VisibleForTesting public Settings(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public static Settings getSettings(Context context) {
        if (mSettings != null) {
            return mSettings;
        }
        mSettings = new Settings(context);
        return mSettings;
    }
}
