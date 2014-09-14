package io.github.fleetc0m.ttyl.observer;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.core.Settings;
import io.github.fleetc0m.ttyl.events.CalendarEvent;
import io.github.fleetc0m.ttyl.events.DrivingEvent;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.events.IncomingCallEvent;
import io.github.fleetc0m.ttyl.events.IncomingSmsEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks whether user is busy, and manage auto reply of incoming call and text when user is busy.
 */
public class AutoReplyController implements EventBus.Observer, EventBus.Updater {
    private static final String TAG = "AutoReplyController";

    public static final String SETTINGS_SHOULD_REPLY_SMS_ON_INCOMING_CALL_IN_BUSY_CALENDAR_EVENT_BOOLEAN =
            "settings-should-reply-sms-on-incoming-call-in-busy-calendar-event-boolean";
    public static final String SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_CALL_IN_BUSY_CALENDAR_EVENT_STRING =
            "settings-auto-reply-sms-content-in-busy-calendar-event-string";

    public static final String SETTING_SHOULD_REPLY_SMS_ON_INCOMING_CALL_WHEN_DRIVING_BOOLEAN =
            "settings-should-reply-sms-on-incoming-call-when-driving-boolean";
    public static final String SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_CALL_WHEN_DRIVING_STRING =
            "settings-auto-reply-sms-content-on-incoming-call-when-driving-boolean";

    public static final String SETTINGS_SHOULD_REPLY_SMS_ON_INCOMING_SMS_IN_BUSY_CALENDAR_EVENT_BOOLEAN =
            "settings-should-reply-sms-on-incoming-sms-in-busy-calendar-event-boolean";
    public static final String SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_SMS_IN_BUSY_CALENDAR_EVENT_STRING =
            "settings-auto-reply-sms-content-on-incoming-sms-in-busy-calendar-event-string";

    public static final String SETTINGS_SHOULD_REPLY_SMS_ON_INCOMING_SMS_WHEN_DRIVING_BOOLEAN =
            "settings-should-reply-sms-on-incoming-sms-when-driving-boolean";
    public static final String SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_SMS_WHEN_DRIVING_STRING =
            "settings-auto-reply-sms-content-on-incoming-sms-when-driving-string";

    private final Context mContext;
    private final EventBus mEventBus;
    private final Settings mSettings;
    private final SmsManager mSmsManager;

    private final Set<String> mOnGoingEventSet;

    public AutoReplyController(Context context, EventBus eventBus, SmsManager smsManager) {
        mOnGoingEventSet = new HashSet<String>();
        mContext = context;
        mEventBus = eventBus;
        mSettings = Settings.getSettings(context);
        mSmsManager = smsManager;
    }

    @Override
    public void onStateChanged(final Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                maybeHandleDrivingEvent(event);
                maybeHandleCalendarEvent(event);
                maybeHandleIncomingCallEvent(event);
            }
        }).start();

    }

    private void maybeHandleDrivingEvent(Event event) {
        if (event.getEventType().equals(Event.DRIVING)) {
            if (event.getAdditionalInfo().getBoolean(DrivingEvent.KEY_DRIVING_BOOLEAN)) {
                mOnGoingEventSet.add(Event.DRIVING);
            } else {
                mOnGoingEventSet.remove(Event.DRIVING);
            }
        }
    }

    private void maybeHandleCalendarEvent(Event event) {
        if (event.getEventType().equals((Event.CALENDAR_ENTRY))) {
            if (event.getAdditionalInfo().getBoolean(CalendarEvent.KEY_BUSY_BOOLEAN)) {
                mOnGoingEventSet.add(Event.CALENDAR_ENTRY);
            } else {
                mOnGoingEventSet.remove(Event.CALENDAR_ENTRY);
            }
        }
    }

    private void maybeHandleIncomingCallEvent(Event event) {
        if (event.getEventType().equals(Event.INCOMING_CALL)) {
            if (mOnGoingEventSet.contains(Event.CALENDAR_ENTRY) &&
                    mSettings.getSharedPreferences().getBoolean(
                            SETTINGS_SHOULD_REPLY_SMS_ON_INCOMING_CALL_IN_BUSY_CALENDAR_EVENT_BOOLEAN, false) &&
                    mSettings.getSharedPreferences().contains(SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_CALL_IN_BUSY_CALENDAR_EVENT_STRING)) {
                String senderAddress = event.getAdditionalInfo().getString(
                        IncomingCallEvent.KEY_INCOMING_NUMBER_STRING);
                String textMessage = getAutoReplySmsContentForIncomingCallOnCalendarEvent(event);
                mSmsManager.sendTextMessage(senderAddress,
                        null,
                        textMessage,
                        null,
                        null);
                Log.d(TAG, String.format(
                        "SMS sent to %s: %s. reason: calendar entry and incoming call",
                        senderAddress, textMessage));
            }
            if (mOnGoingEventSet.contains(Event.DRIVING) &&
                    mSettings.getSharedPreferences().getBoolean(
                            SETTING_SHOULD_REPLY_SMS_ON_INCOMING_CALL_WHEN_DRIVING_BOOLEAN, false)&&
                    mSettings.getSharedPreferences().contains(SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_CALL_WHEN_DRIVING_STRING)) {
                String senderAddress = event.getAdditionalInfo().getString(
                        IncomingCallEvent.KEY_INCOMING_NUMBER_STRING);
                String textMessage = getSettingsAutoReplySmsContentForIncomingCallOnDrivingEvent(event);
                mSmsManager.sendTextMessage(senderAddress,
                        null,
                        textMessage,
                        null,
                        null);
                Log.d(TAG, String.format("SMS sent to %s: %s. reason: driving and incoming call",
                        senderAddress, textMessage));
            }
        }
    }

    private void maybeHandleIncomingSmsEvent(Event event) {
        if (event.getEventType().equals(Event.INCOMING_SMS)) {
            if (mOnGoingEventSet.contains(Event.CALENDAR_ENTRY) &&
                    mSettings.getSharedPreferences().getBoolean(SETTINGS_SHOULD_REPLY_SMS_ON_INCOMING_SMS_IN_BUSY_CALENDAR_EVENT_BOOLEAN, false) &&
                    mSettings.getSharedPreferences().contains(SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_CALL_IN_BUSY_CALENDAR_EVENT_STRING)) {
                String senderAddress = event.getAdditionalInfo().getString(
                        IncomingSmsEvent.KEY_SENDER_ADDRESS_STRING);
                String textMessage = getAutoReplySmsContentForIncomingSmsOnCalendarEvent(event);
                mSmsManager.sendTextMessage(senderAddress, null, textMessage, null, null);
                Log.d(TAG, String.format("SMS sent to %s: %s. reason: calendar and incoming sms.",
                        senderAddress, textMessage));
            }
            if (mOnGoingEventSet.contains(Event.DRIVING) &&
                    mSettings.getSharedPreferences().getBoolean(SETTINGS_SHOULD_REPLY_SMS_ON_INCOMING_SMS_WHEN_DRIVING_BOOLEAN, false) &&
                    mSettings.getSharedPreferences().contains(SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_SMS_WHEN_DRIVING_STRING)) {
                String senderAddress = event.getAdditionalInfo().getString(IncomingSmsEvent.KEY_SENDER_ADDRESS_STRING);
                String textMessage = getAutoReplySmsContentForIncomingSmsOnDrivingEvent(event);
                mSmsManager.sendTextMessage(senderAddress, null, textMessage, null, null);
                Log.d(TAG, String.format("SMS sent to %s: %s. reason: driving and incoming sms",
                        senderAddress, textMessage));
            }
        }
    }

    private String getAutoReplySmsContentForIncomingCallOnCalendarEvent(Event event) {
        return mSettings.getSharedPreferences().getString(
                SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_CALL_IN_BUSY_CALENDAR_EVENT_STRING,
                null);
    }

    private String getSettingsAutoReplySmsContentForIncomingCallOnDrivingEvent(Event event) {
        return mSettings.getSharedPreferences().getString(
                SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_CALL_WHEN_DRIVING_STRING, null);
    }

    private String getAutoReplySmsContentForIncomingSmsOnCalendarEvent(Event event) {
        return mSettings.getSharedPreferences().getString(
                SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_SMS_IN_BUSY_CALENDAR_EVENT_STRING, null);
    }

    private String getAutoReplySmsContentForIncomingSmsOnDrivingEvent(Event event) {
        return mSettings.getSharedPreferences().getString(
                SETTINGS_AUTO_REPLY_SMS_CONTENT_ON_INCOMING_SMS_WHEN_DRIVING_STRING, null);
    }

    @Override
    public boolean shouldResponseTo(String eventType) {
        if (eventType.equals(Event.INCOMING_CALL) ||
                eventType.equals(Event.INCOMING_SMS) ||
                eventType.equals(Event.CALENDAR_ENTRY) ||
                eventType.equals(Event.DRIVING)) {
            return true;
        }
        return false;
    }
}
