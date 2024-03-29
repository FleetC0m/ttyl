package io.github.fleetc0m.ttyl.updater;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.core.Settings;
import io.github.fleetc0m.ttyl.events.DrivingEvent;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.util.DrivingStateUtil;

/**
 * Monitor whether user is driving.
 *
 * If the user is moving faster than a threshold, a DrivingEvent
 * is committed onto the EventBus. This doesn't work well because driving is a continuous action and
 * we should use a more resilient way to detect whether user is driving, like the average speed
 * in a five minute period.
 */
public class DrivingStateUpdater implements EventBus.Updater, EventBus.Observer, Runnable {
    /** The lower limit of speed which considered driving, in mph */
    public static final String SETTING_SPEED_THRESHOLD_INT =
            "driving-state-updater-setting-speed-threshold-int";

    private static final String TAG = "DrivingStateUpdater";
    /** Request location update for each interval */
    private static final long INTERVAL_MS = 5 * DateUtils.MINUTE_IN_MILLIS;
    private static final int DEFAULT_SPEED_MPH_THRESHOLD = 40;

    private final Context mContext;
    private final EventBus mEventBus;
    private final LocationManager mLocationManager;
    private final DrivingStateUtil mDrivingStateUtil;
    private final Settings mSettings;

    @VisibleForTesting public final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location.hasSpeed()) {
                final double speedMph = mDrivingStateUtil.meterPerSecToMilePerHour(
                        location.getSpeed());
                Log.d(TAG, String.format("speed = %.1f MPH", speedMph));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if ((speedMph > getSpeedThresholdMph()) && !mUserWasDriving) {
                            mUserWasDriving = true;
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(DrivingEvent.KEY_DRIVING_BOOLEAN, true);
                            bundle.putDouble(DrivingEvent.KEY_DRIVING_SPEED_DOUBLE, speedMph);
                            DrivingEvent drivingEvent = new DrivingEvent(bundle);
                            Log.d(TAG, "committing driving event, driving = true");
                            mEventBus.onStateChanged(drivingEvent);
                        }
                        else if ((speedMph < getSpeedThresholdMph()) && mUserWasDriving) {
                            mUserWasDriving = false;
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(DrivingEvent.KEY_DRIVING_BOOLEAN, false);
                            DrivingEvent drivingEvent = new DrivingEvent(bundle);
                            Log.d(TAG, "committing driving event, driving = false");
                            mEventBus.onStateChanged(drivingEvent);
                        }
                    }
                }).start();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    private boolean mShouldRun;

    /** If the user is moving faster than this speed in MPH */
    private volatile double mSpeedThreshold;

    /** Whether user was driving at previous moment */
    private volatile boolean mUserWasDriving;

    public DrivingStateUpdater(Context context, EventBus eventBus) {
        mContext = context;
        mEventBus = eventBus;
        mShouldRun = true;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mDrivingStateUtil = new DrivingStateUtil(mContext);
        mUserWasDriving = false;
        mSettings = Settings.getSettings(context);
        new Thread(this).start();
    }

    @VisibleForTesting public DrivingStateUpdater(
            Context context, EventBus eventBus, Settings settings) {
        mContext = context;
        mEventBus = eventBus;
        mShouldRun = true;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mDrivingStateUtil = new DrivingStateUtil(mContext);
        mUserWasDriving = false;
        mSettings = settings;
        new Thread(this).start();
    }

    private int getSpeedThresholdMph() {
        return mSettings.getSharedPreferences().getInt(SETTING_SPEED_THRESHOLD_INT,
                DEFAULT_SPEED_MPH_THRESHOLD);
    }

    @Override
    public void run() {
        Looper.prepare();
        Looper.loop();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                INTERVAL_MS, 0, mLocationListener);
    }

    @Override
    public void onStateChanged(Event event) {
        // TODO: stop listening to location update when requested.
    }

    @Override
    public boolean shouldResponseTo(String eventType) {
        if (eventType.equals(Event.QUIT)) {
            return true;
        }
        return false;
    }
}
