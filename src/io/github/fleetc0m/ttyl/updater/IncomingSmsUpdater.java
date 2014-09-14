package io.github.fleetc0m.ttyl.updater;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.IncomingSmsEvent;

/**
 * Commit an IncomingSmsEvent onto the EventBus on receiving an incoming SMS.
 */
public class IncomingSmsUpdater extends BroadcastReceiver implements EventBus.Updater {
    private static final String TAG = "IncomingSmsUpdater";

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            for (final SmsMessage message : messages) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        EventBus mEventBus = EventBus.getEventBus();
                        IncomingSmsEvent event = getIncomingSmsEvent(message);
                        mEventBus.onStateChanged(event);
                    }
                }).start();
            }
        }
    }

    private IncomingSmsEvent getIncomingSmsEvent(SmsMessage message) {
        Bundle  bundle = new Bundle();
        bundle.putString(
                IncomingSmsEvent.KEY_SENDER_ADDRESS_STRING, message.getOriginatingAddress());
        bundle.putString(IncomingSmsEvent.KEY_MESSAGE_BODY_STRING, message.getDisplayMessageBody());
        IncomingSmsEvent event = new IncomingSmsEvent(bundle);
        return event;
    }
}
