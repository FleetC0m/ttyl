package io.github.fleetc0m.ttyl.observer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.core.Settings;
import io.github.fleetc0m.ttyl.events.CalendarEvent;
import io.github.fleetc0m.ttyl.events.DrivingEvent;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.events.RingerControlEvent;

import java.util.HashMap;

/**
 * Decides whether to issue RingerControlEvent to mute or unmute the ringer.
 */
public class RingerController implements EventBus.Observer, EventBus.Updater {
    private static final String TAG = "RingerController";

    public static final String SETTINGS_MUTE_ALL_BUSY_CALENDAR_EVENT_BOOLEAN =
            "settings-should-mute-all-busy-calendar-event-boolean";
    public static final String SETTINGS_SHOULD_MUTE_BY_CALENDAR_ID_BOOLEAN_TEMPLATE =
            "settings-should-mute-calendar-%d-boolean";

    public static final String SETTINGS_SHOULD_MUTE_WHEN_DRIVING_BOOLEAN =
            "settings-should-mute-when-driving-boolean";

    private final Context mContext;
    private final EventBus mEventBus;
    private final Settings mSettings;
    @VisibleForTesting final HashMap<String, Boolean> mEventMap;

    public RingerController(Context context, EventBus eventBus) {
        mContext = context;
        mEventBus = eventBus;
        mSettings = Settings.getSettings(mContext);
        mEventMap = new HashMap<String, Boolean>();
    }

    @VisibleForTesting public RingerController(
            Context context, EventBus eventBus, Settings settings) {
        mContext = context;
        mEventBus = eventBus;
        mEventMap = new HashMap<String, Boolean>();
        mSettings = settings;
    }

    @Override
    public void onStateChanged(final Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (event.getEventType().equals(Event.CALENDAR_ENTRY)) {
                    if (shouldMuteForCalendarEvent(event)) {
                        Log.d(TAG, "shouldMuteForCalendarEvent");
                        mEventMap.put(Event.CALENDAR_ENTRY, true);
                        maybeCommitMuteEvent();
                    } else if (shouldUnmuteForCalendarEvent(event)) {
                        Log.d(TAG, "shouldUnmuteForCalendarEvent");
                        mEventMap.remove(Event.CALENDAR_ENTRY);
                        maybeCommitUnmuteEvent();
                    }
                } else if (event.getEventType().equals(Event.DRIVING)) {
                    if (shouldMuteForDriving(event)) {
                        Log.d(TAG, "shouldMuteForDrivingEvent");
                        mEventMap.put(Event.DRIVING, true);
                        maybeCommitMuteEvent();
                    } else if (shouldUnmuteForDriving(event)) {
                        Log.d(TAG, "shouldUnmuteForDrivingEvent");
                        mEventMap.remove(Event.DRIVING);
                        maybeCommitUnmuteEvent();
                    }
                }
            }
        }).start();
    }

    /**
     * We need to make a design decision here. Consider the following sequence of event:
     * Calendar busy, mute -> user unmutes -> driving. When the DrivingEvent happens, do we mute or
     * not? If we need to mute, then we should always commit a RingerControlEvent regardless the
     * size of the mEventMap. Otherwise we only mute when it's the only entry in the mEventMap
     */
    private void maybeCommitMuteEvent() {
        if (mEventMap.size() == 1) {
            Bundle bundle = new Bundle();
            bundle.putInt(RingerControlEvent.KEY_RINGER_MODE_INT,
                    AudioManager.RINGER_MODE_SILENT);
            RingerControlEvent ringerControlEvent = new RingerControlEvent(bundle);
            mEventBus.onStateChanged(ringerControlEvent);
        }
    }

    private void maybeCommitUnmuteEvent() {
        if (mEventMap.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(RingerControlEvent.KEY_RINGER_MODE_REVERT_BOOLEAN, true);
            RingerControlEvent ringerControlEvent = new RingerControlEvent(bundle);
            mEventBus.onStateChanged(ringerControlEvent);
        }
    }

    @VisibleForTesting boolean shouldMuteForCalendarEvent(Event calendarEvent) {
        if (mSettings.getSharedPreferences().getBoolean(
                SETTINGS_MUTE_ALL_BUSY_CALENDAR_EVENT_BOOLEAN, false) &&
                calendarEvent.getAdditionalInfo().containsKey(CalendarEvent.KEY_BUSY_BOOLEAN) &&
                calendarEvent.getAdditionalInfo().getBoolean(CalendarEvent.KEY_BUSY_BOOLEAN)) {
            return true;
        }
        if (calendarEvent.getAdditionalInfo().containsKey(CalendarEvent.KEY_CALENDAR_ID_INT) &&
                mSettings.getSharedPreferences().getBoolean(
                        String.format(
                                SETTINGS_SHOULD_MUTE_BY_CALENDAR_ID_BOOLEAN_TEMPLATE,
                                calendarEvent.getAdditionalInfo().getInt(
                                        CalendarEvent.KEY_CALENDAR_ID_INT)),
                        false)
                ) {
            return true;
        }
        // TODO: implement keyword matching in calendar title
        return false;
    }

    @VisibleForTesting boolean shouldUnmuteForCalendarEvent(Event calendarEvent) {
        if (calendarEvent.getAdditionalInfo().containsKey(CalendarEvent.KEY_BUSY_BOOLEAN) &&
                !calendarEvent.getAdditionalInfo().getBoolean(CalendarEvent.KEY_BUSY_BOOLEAN)) {
            return true;
        }
        return false;
    }

    @VisibleForTesting boolean shouldMuteForDriving(Event drivingEvent) {
        if (drivingEvent.getAdditionalInfo().containsKey(DrivingEvent.KEY_DRIVING_BOOLEAN) &&
                mSettings.getSharedPreferences().getBoolean(
                        SETTINGS_SHOULD_MUTE_WHEN_DRIVING_BOOLEAN, false)) {
            return true;
        }
        return false;
    }

    @VisibleForTesting boolean shouldUnmuteForDriving(Event drivingEvent) {
        if (!drivingEvent.getAdditionalInfo().getBoolean(DrivingEvent.KEY_DRIVING_BOOLEAN)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldResponseTo(String eventType) {
        if (eventType.equals(Event.DRIVING) ||
                eventType.equals(Event.CALENDAR_ENTRY)) {
            return true;
        }
        return false;
    }
}
