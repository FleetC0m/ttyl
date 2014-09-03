package io.github.fleetc0m.ttyl.observer;

import android.content.Context;
import android.util.Log;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.events.IncomingCallEvent;

/**
 * Listens incoming call event.
 */
public class IncomingCallObserver implements EventBus.Observer {
    private static final String TAG = "IncomingCallObserver";

    private final Context mContext;
    private final EventBus mEventBus;

    public IncomingCallObserver(Context context, EventBus eventBus) {
        mContext = context;
        mEventBus = eventBus;
    }

    @Override
    public void onStateChanged(Event event) {
        Log.d(TAG, String.format("incoming call: %s", event.getAdditionalInfo().getString(
                IncomingCallEvent.KEY_INCOMING_NUMBER_STRING)));
    }

    @Override
    public boolean shouldResponseTo(String eventType) {
        if (eventType.equals(Event.INCOMING_CALL)) {
            return true;
        }
        return false;
    }
}
