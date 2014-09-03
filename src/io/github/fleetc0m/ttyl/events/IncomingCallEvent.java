package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * Created by Icarus on 9/2/14.
 */
public class IncomingCallEvent extends Event {
    public static final String KEY_INCOMING_NUMBER_STRING = "incoming-number-string";

    private final Bundle mBundle;

    public IncomingCallEvent(Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public String getEventType() {
        return Event.INCOMING_CALL;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return mBundle;
    }
}
