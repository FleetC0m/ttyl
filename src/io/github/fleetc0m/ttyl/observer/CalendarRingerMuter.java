package io.github.fleetc0m.ttyl.observer;

import android.content.Context;
import android.media.AudioManager;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.CalendarEvent;
import io.github.fleetc0m.ttyl.events.Event;

/**
 * Mute the phone ringer if user is busy according to the calendar.
 */
public class CalendarRingerMuter implements EventBus.Observer{
    private final Context mContext;

    private int mOriginalRingerMode;

    /** Whether we silenced the ringer before. If we silenced it before, we should revert, otherwise
     *  don't revert.
     */
    private boolean mRingerModified;

    public CalendarRingerMuter(Context context) {
        mContext = context;
        mRingerModified = false;
    }

    @Override
    public void onStateChanged(Event event) {
        if (event.getEventType() == Event.AGENDA) {
            AudioManager audioManager = (AudioManager) mContext.getSystemService(
                    Context.AUDIO_SERVICE);
            // TODO: check if user wants to mute the ringer when busy
            if (event.getAdditionalInfo().getBoolean(CalendarEvent.KEY_BUSY_BOOLEAN)) {
                // user is currently busy, mute the phone.
                // TODO: log this action.
                mOriginalRingerMode = audioManager.getRingerMode();
                if (mOriginalRingerMode != AudioManager.RINGER_MODE_SILENT) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                mRingerModified = true;
            } else {
                if (mRingerModified) {
                    // only revert the ringer if we modified it before.
                    audioManager.setRingerMode(mOriginalRingerMode);
                    mRingerModified = false;
                }
            }
        }
    }

    @Override
    public boolean shouldResponseTo(int eventType) {
        if (eventType == Event.AGENDA) {
            return true;
        }
        return false;
    }
}
