package io.github.fleetc0m.ttyl.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.IncomingCallEvent;

/**
 * Commit a IncomeCallEvent to the EventBus when.. there's a call incoming.
 */
public class IncomingCallUpdater extends BroadcastReceiver implements EventBus.Updater {
    private static final String TAG = "IncomingCallUpdater";

    private IncomingCallListener mIncomingCallListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        mIncomingCallListener = new IncomingCallListener();
        telephonyManager.listen(mIncomingCallListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private static class IncomingCallListener extends PhoneStateListener {
        private final EventBus mEventBus;

        public IncomingCallListener() {
            mEventBus = EventBus.getEventBus();
        }

        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putString(IncomingCallEvent.KEY_INCOMING_NUMBER_STRING,
                                incomingNumber);
                        IncomingCallEvent incomingCallEvent = new IncomingCallEvent(bundle);
                        mEventBus.onStateChanged(incomingCallEvent);
                    }
                }).start();
            }
        }
    }
}
