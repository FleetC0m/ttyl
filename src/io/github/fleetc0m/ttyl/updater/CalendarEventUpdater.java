package io.github.fleetc0m.ttyl.updater;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.CalendarEvent;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.util.Clock;

/**
 * Handles querying calendar for ongoing event repeatedly every certain interval. If it finds an
 * ongoing event that is deemed to be busy, it notifies the event bus with information about this
 * calendar event. It stops pulling event when it sees an app quit event.
 */
public class CalendarEventUpdater implements EventBus.Updater, EventBus.Observer, Runnable {
    private static final String TAG = "CalendarEventUpdater";
    private static final String[] PROJECTION = new String[]{CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.SELF_ATTENDEE_STATUS,
            CalendarContract.Events.AVAILABILITY,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.CALENDAR_ID};

    /** Look up the calendar provider every INTERVAL_MS */
    private static final long INTERVAL_MS = 2 * DateUtils.SECOND_IN_MILLIS;

    private final Clock mClock;
    private final Context mContext;
    private final EventBus mEventBus;
    private volatile boolean mShouldRun;

    /** The previous status (true = busy/false = free). We only send out event if we see a change
     * to the state.
     */
    private boolean mPreviousState;

    public CalendarEventUpdater(Clock clock, Context context, EventBus eventBus) {
        mClock = clock;
        mContext = context;
        mEventBus = eventBus;
        mShouldRun = true;
        mPreviousState = false;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while(mShouldRun) {
            maybeNotifyEventBusForOngoingEvent();
            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                // Nothing
            }
        }
    }

    /**
     * Queries the calendar provider, and commit an event to the event bus with calendar entry
     * details if a calendar entry in which the user is busy is going on.
     */
    private void maybeNotifyEventBusForOngoingEvent() {
        Bundle bundle = queryCurrentOngoingEvent();
        if (bundle != null) {
            CalendarEvent calendarEvent = new CalendarEvent(bundle);
            mEventBus.onStateChanged(calendarEvent);
        }
    }

    /**
     * @return Return any ongoing calendar event info where the user is deemed to be
     * busy, or null if the user is free at this time.
     */
    private Bundle queryCurrentOngoingEvent() {
        Cursor cursor = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        String selection = String.format("%s <= ? AND %s >= ?",
                CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND);
        long currentTimeMs = mClock.currentTimeMillis();
        String currentTimeMsStr = Long.toString(currentTimeMs);
        String[] selectionArgs = new String[] {currentTimeMsStr, currentTimeMsStr};
        Uri uri = CalendarContract.Events.CONTENT_URI;
        cursor = contentResolver.query(uri,
                PROJECTION,
                selection,
                selectionArgs,
                CalendarContract.Events.DTSTART);
        if (cursor == null) {
            Log.wtf(TAG, "query failed");
            return null;
        }
        Log.d(TAG, String.format("cursor returned %d items.", cursor.getCount()));
        Bundle bundle = new Bundle();
        while (cursor.moveToNext()) {
            String descriptionStr = cursor.getString(
                    cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
            String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
            int selfAttendeeStatus = cursor.getInt(
                    cursor.getColumnIndex(CalendarContract.Events.SELF_ATTENDEE_STATUS));
            int availability = cursor.getInt(
                    cursor.getColumnIndex(CalendarContract.Events.AVAILABILITY));
            int calendarId = cursor.getInt(cursor.getColumnIndex(
                    CalendarContract.Events.CALENDAR_ID));
            if (isBusy(selfAttendeeStatus, availability) && (mPreviousState == false)) {
                mPreviousState = true;
                bundle.putString(CalendarEvent.KEY_CALENDAR_DESCRIPTION_STRING, descriptionStr);
                bundle.putString(CalendarEvent.KEY_CALENDAR_TITLE_STRING, title);
                bundle.putInt(CalendarEvent.KEY_CALENDAR_ID_INT, calendarId);
                bundle.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, true);
                bundle.putBoolean(Event.KEY_USER_BUSY_BOOLEAN, true);
                Log.d(TAG, String.format("user is busy with %s", title));
                return bundle;
            }
        }
        if ((cursor.getCount() == 0) && (mPreviousState == true)) {
            Log.d(TAG, "user is now free");
            mPreviousState = false;
            bundle.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, false);
            bundle.putBoolean(Event.KEY_USER_BUSY_BOOLEAN, false);
            return bundle;
        }
        return null;
    }

    private boolean isBusy(int selfAttendeeStatus, int availability) {
        if (selfAttendeeStatus == CalendarContract.Events.STATUS_CONFIRMED ||
                selfAttendeeStatus == CalendarContract.Events.STATUS_TENTATIVE) {
            return true;
        }

        if (availability == CalendarContract.Events.AVAILABILITY_BUSY ||
                availability == CalendarContract.Events.AVAILABILITY_TENTATIVE) {
            return true;
        }
        return false;
    }

    @Override
    public void onStateChanged(Event event) {
        if (event.getEventType().equals(Event.QUIT)) {
            mShouldRun = false;
        }
    }

    @Override
    public boolean shouldResponseTo(String eventType) {
        if (eventType.equals(Event.QUIT)) {
            return true;
        }
        return false;
    }
}
