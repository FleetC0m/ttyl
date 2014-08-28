package io.github.fleetc0m.ttyl.util;

import android.content.Context;

/**
 * Created by Icarus on 8/28/14.
 */
public class DrivingStateUtil {
    private final Context mContext;

    public DrivingStateUtil(Context context) {
        mContext = context;
    }

    public double meterPerSecToMilePerHour(double mps) {
        return mps * 2.23693629;
    }

    public double milePerHourToMeterPerSec(double mph) {
        return mph / 2.23693629;
    }
}
