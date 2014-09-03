package io.github.fleetc0m.ttyl.observer;

import android.content.Context;
import android.media.AudioManager;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.events.RingerControlEvent;

/**
 * Mute/unmute the ringer according to the RingerControlEvent.
 */
public class RingerMuter implements EventBus.Observer {
    private final Context mContext;
    private final EventBus mEventBus;

    private int mOrigRingerMode;

    public RingerMuter(Context context, EventBus eventBus) {
        mContext = context;
        mEventBus = eventBus;
    }

    @Override
    public void onStateChanged(final Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (event.getAdditionalInfo().containsKey(RingerControlEvent.KEY_RINGER_MODE_INT)) {
                    setRingerMode(event.getAdditionalInfo().getInt(
                            RingerControlEvent.KEY_RINGER_MODE_INT));
                } else if (event.getAdditionalInfo().getBoolean(
                        RingerControlEvent.KEY_RINGER_MODE_REVERT_BOOLEAN, false)) {
                    revert();
                }
            }
        }).start();
    }

    private void setRingerMode(int ringerMode) {
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mOrigRingerMode = am.getRingerMode();
        am.setRingerMode(ringerMode);
    }

    private void mute() {
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mOrigRingerMode = am.getRingerMode();
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    private void revert() {
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.setRingerMode(mOrigRingerMode);
    }

    @Override
    public boolean shouldResponseTo(String eventType) {
        if (eventType.equals(Event.RINGER_CONTROL)) {
            return true;
        }
        return false;
    }
}
