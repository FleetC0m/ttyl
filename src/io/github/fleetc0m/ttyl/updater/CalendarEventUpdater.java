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

import java.util.Calendar;

/**
 * Created by Icarus on 8/24/14.
 */
public class CalendarEventUpdater implements EventBus.Updater, Runnable{

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

        while (cursor.moveToNext()) {
            String descriptionStr = cursor.getString(
                    cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
            int selfAttendeeStatus = cursor.getInt(
                    cursor.getColumnIndex(CalendarContract.Events.SELF_ATTENDEE_STATUS));
            int availability = cursor.getInt(
                    cursor.getColumnIndex(CalendarContract.Events.AVAILABILITY));

        }
    }
}
