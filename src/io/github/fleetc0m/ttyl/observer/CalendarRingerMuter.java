package io.github.fleetc0m.ttyl.observer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.CalendarEvent;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.events.RingerChangeEvent;

/**
 * Mute the phone ringer if user is busy according to the calendar.
 *
 * There could be multiple muters for different events. For example, there is a CalendarRingerMuter,
 * which mutes the phone when there's currently a calendar event going on that the user is busy,
 * and there is also a DrivingMuter, which mutes the phone when user is currently driving and don't
 * want to respond to incoming calls.
 *
 * The problem is there could be conflicts between the muters. For example, we discover there's a
 * calendar event going on, we mute the phone. Then we discover the user is driving, then the
 * DrivingMuter mutes the phone. Later we discover the user is no longer driving. But at this time
 * we can not unmute the phone because the calendar event is still going on!
 *
 * The current solution to this problem is instead of unmuting the phone, we revert the ringer back
 * to its original state. So in the example above when we discover that the user is driving, we
 * remember the previous ringer mode, which is silence, then we silence the phone (or do nothing).
 * When we discover that the user is no longer driving, we revert the ringer to the previous state,
 * which is still silence. This should solve the conflict stated above, and I can't think of any
 * way to break it.
 */
public class CalendarRingerMuter implements EventBus.Observer, EventBus.Updater{
    private static final String TAG = "CalendarRingerMuter";

    private final Context mContext;

    private int mOriginalRingerMode;

    /** Whether we silenced the ringer before. If we silenced it before, we should revert, otherwise
     *  don't revert.
     */
    private boolean mRingerModified;
    private final EventBus mEventBus;

    public CalendarRingerMuter(@NonNull Context context, @NonNull EventBus eventBus) {
        mContext = context;
        mRingerModified = false;
        mEventBus = eventBus;
    }

    @Override
    public void onStateChanged(final Event event) {
        Log.d(TAG, String.format("onStateChanged, event %s", event.getEventType()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (event.getEventType().equals(Event.CALENDAR_ENTRY)) {
                    AudioManager audioManager = (AudioManager) mContext.getSystemService(
                            Context.AUDIO_SERVICE);
                    Bundle additionalInfo = event.getAdditionalInfo();
                    int currentRingerMode = audioManager.getRingerMode();
                    Log.d(TAG, String.format("currentRingerMode = %d", currentRingerMode));
                    Log.d(TAG, String.format("additionalInfo.containsKey(%s) = %b",
                            CalendarEvent.KEY_BUSY_BOOLEAN,
                            additionalInfo.containsKey(CalendarEvent.KEY_BUSY_BOOLEAN)));
                    Log.d(TAG, String.format("additionalInfo.getBoolean(%s) = %b",
                            CalendarEvent.KEY_BUSY_BOOLEAN,
                            additionalInfo.getBoolean(CalendarEvent.KEY_BUSY_BOOLEAN)));

                    if (additionalInfo.containsKey(CalendarEvent.KEY_BUSY_BOOLEAN) &&
                            additionalInfo.getBoolean(CalendarEvent.KEY_BUSY_BOOLEAN) &&
                            currentRingerMode != AudioManager.RINGER_MODE_SILENT) {
                        Log.d(TAG, String.format("user busy, current ringer %d, silencing ringer.",
                                        currentRingerMode));
                        mOriginalRingerMode = currentRingerMode;
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        RingerChangeEvent ringerChangeEvent = new RingerChangeEvent(
                                mOriginalRingerMode, AudioManager.RINGER_MODE_SILENT);
                        mEventBus.onStateChanged(ringerChangeEvent);
                    } else if (additionalInfo.containsKey(CalendarEvent.KEY_BUSY_BOOLEAN) &&
                            !additionalInfo.getBoolean(CalendarEvent.KEY_BUSY_BOOLEAN) &&
                            mOriginalRingerMode != AudioManager.RINGER_MODE_SILENT) {
                        audioManager.setRingerMode(mOriginalRingerMode);
                        RingerChangeEvent ringerChangeEvent = new RingerChangeEvent(
                                AudioManager.RINGER_MODE_SILENT, mOriginalRingerMode);
                        mEventBus.onStateChanged(ringerChangeEvent);
                    }
                }
            }
        }).start();
    }

    private void mute() {

    }

    @Override
    public boolean shouldResponseTo(String eventType) {
        if (eventType.equals(Event.CALENDAR_ENTRY)) {
            return true;
        }
        return false;
    }
}
