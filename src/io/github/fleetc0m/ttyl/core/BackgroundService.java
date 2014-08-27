package io.github.fleetc0m.ttyl.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import io.github.fleetc0m.ttyl.HomeActivity;
import io.github.fleetc0m.ttyl.observer.CalendarRingerMuter;
import io.github.fleetc0m.ttyl.updater.CalendarEventUpdater;
import io.github.fleetc0m.ttyl.util.Clock;
import io.github.fleetc0m.ttyl.util.ClockImpl;

/**
 * Created on boot complete or when user first open the app and decide to start the service.
 */
public class BackgroundService extends Service{
    private static final String TAG = "BackgroundService";
    private static final int ONGOING_NOTIFICATION = 1;

    private EventBus mEventBus;
    private Clock mClock;
    private CalendarEventUpdater mCalendarEventUpdater;
    private CalendarRingerMuter mCalendarRingerMuter;

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = new Notification();
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        startForeground(ONGOING_NOTIFICATION, notification);

        init();
        registerObservers();
        registerUpdaters();
    }

    private void init() {
        mEventBus = EventBus.getEventBus();
        mClock = new ClockImpl();
    }

    private void registerUpdaters() {
        mCalendarEventUpdater = new CalendarEventUpdater(mClock, this, mEventBus);
        mEventBus.registerUpdater(mCalendarEventUpdater);
    }

    private void registerObservers() {
        mCalendarRingerMuter = new CalendarRingerMuter(this, mEventBus);
        mEventBus.registerObserver(mCalendarRingerMuter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Does not allow binding
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}
