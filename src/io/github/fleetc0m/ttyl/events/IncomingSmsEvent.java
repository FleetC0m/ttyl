package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * Created by Icarus on 9/13/14.
 */
public class IncomingSmsEvent extends Event {
    public static final String KEY_SENDER_ADDRESS_STRING =
            "incoming-sms-event-sender-address-string";
    public static final String KEY_MESSAGE_BODY_STRING =
            "incoming-sms-event-message-body-string";

    private final Bundle mBundle;

    public IncomingSmsEvent(Bundle bundle) {
        mBundle = bundle;
    }
    @Override
    public String getEventType() {
        return Event.INCOMING_SMS;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return mBundle;
    }
}
