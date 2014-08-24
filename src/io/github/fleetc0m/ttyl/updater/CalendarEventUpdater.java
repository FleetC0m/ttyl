package io.github.fleetc0m.ttyl.updater;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.util.Clock;

/**
 * Handles querying calendar for ongoing event repeatedly every certain interval. If it finds an
 * ongoing event that is deemed to be busy, it notifies the event bus with information about this
 * calendar event.
 */
public class CalendarEventUpdater implements EventBus.Updater, Runnable{
    private static final String TAG = "CalendarEventUpdater";
    private static final String[] PROJECTION = new String[]{CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.SELF_ATTENDEE_STATUS,
            CalendarContract.Events.AVAILABILITY};
    private final Clock mClock;
    private final Context mContext;
    private final EventBus mEventBus;

    public CalendarEventUpdater(Clock clock, Context context, EventBus eventBus) {
        mClock = clock;
        mContext = context;
        mEventBus = eventBus;
    }

    @Override
    public void run() {

    }

    /**
     * Queries the calendar provider, and commit an event to the event bus with calendar entry
     * details if a calendar entry in which the user is busy is going on.
     */
    public void maybeNotifyEventBusforOngoingEvent() {

    }

    private Bundle queryCurrentNonRecurrentOngoingEvent() {
        Cursor cursor = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        String selection = String.format("%s <= ? AND %s >= ?",
                CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND);
        String currentTimeMsStr = Long.toString(mClock.currentTimeMillis());
        String[] selectionArgs = new String[] {currentTimeMsStr, currentTimeMsStr};
        Uri uri = CalendarContract.Events.CONTENT_URI;
        cursor = contentResolver.query(uri, PROJECTION, selection, selectionArgs, null);
        if (cursor == null) {
            Log.wtf(TAG, "query failed");
            return null;
        }

        Bundle bundle = new Bundle();
        while (cursor.moveToNext()) {
            String descriptionStr = cursor.getString(
                    cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
            int selfAttendeeStatus = cursor.getInt(
                    cursor.getColumnIndex(CalendarContract.Events.SELF_ATTENDEE_STATUS));
            int availability = cursor.getInt(
                    cursor.getColumnIndex(CalendarContract.Events.AVAILABILITY));
            if (isBusy(selfAttendeeStatus, availability)) {
                // bundle.putString();
            }
        }
        return bundle;
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
}
