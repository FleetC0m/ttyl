package io.github.fleetc0m.ttyl.observer;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.test.AndroidTestCase;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.core.Settings;
import io.github.fleetc0m.ttyl.events.CalendarEvent;
import io.github.fleetc0m.ttyl.events.DrivingEvent;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.events.RingerControlEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link io.github.fleetc0m.ttyl.observer.RingerController}
 */
public class RingerControllerTest extends AndroidTestCase {

    @Mock private Settings mMockSettings;
    @Mock private SharedPreferences mMockSharedPreferences;
    @Mock private EventBus.Observer mMockObserver;

    private EventBus mEventBus;

    private RingerController mRingerController;

    @Before
    @Override
    public void setUp() {
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
        MockitoAnnotations.initMocks(this);
        when(mMockObserver.shouldResponseTo(any(String.class))).thenReturn(true);
    }

    @Test
    public void testOnStateChanged_CalendarEventShouldMute() {
        when(mMockSettings.getSharedPreferences()).thenReturn(mMockSharedPreferences);
        when(mMockSharedPreferences.getBoolean(
                eq(RingerController.SETTINGS_MUTE_ALL_BUSY_CALENDAR_EVENT_BOOLEAN),
                any(Boolean.class))).thenReturn(true);
        mEventBus = EventBus.getEventBus();
        mEventBus.reset();
        mEventBus.registerObserver(mMockObserver);
        mRingerController = new RingerController(getContext(), mEventBus, mMockSettings);
        mEventBus.registerUpdater(mRingerController);
        mEventBus.registerObserver(mRingerController);
        Bundle bundle = new Bundle();
        bundle.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, true);
        CalendarEvent calendarEvent = new CalendarEvent(bundle);
        mEventBus.onStateChanged(calendarEvent);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        assertTrue(mRingerController.mEventMap.get(Event.CALENDAR_ENTRY));
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(mMockObserver, times(2)).onStateChanged(captor.capture());
        Event ringerControlEvent = captor.getValue();
        assertEquals(Event.RINGER_CONTROL, ringerControlEvent.getEventType());
        assertTrue(ringerControlEvent.getAdditionalInfo().containsKey(
                RingerControlEvent.KEY_RINGER_MODE_INT));
        assertEquals(AudioManager.RINGER_MODE_SILENT, ringerControlEvent.getAdditionalInfo().getInt(
                RingerControlEvent.KEY_RINGER_MODE_INT));


    }

    @Test
    public void testOnStateChanged_CalendarEventMuteAndUnmute() {
        when(mMockSettings.getSharedPreferences()).thenReturn(mMockSharedPreferences);
        when(mMockSharedPreferences.getBoolean(
                eq(RingerController.SETTINGS_MUTE_ALL_BUSY_CALENDAR_EVENT_BOOLEAN),
                any(Boolean.class))).thenReturn(true);
        mEventBus = EventBus.getEventBus();
        mEventBus.reset();
        mEventBus.registerObserver(mMockObserver);
        mRingerController = new RingerController(getContext(), mEventBus, mMockSettings);
        mEventBus.registerUpdater(mRingerController);
        mEventBus.registerObserver(mRingerController);
        Bundle bundle = new Bundle();
        bundle.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, true);
        CalendarEvent calendarEvent = new CalendarEvent(bundle);
        mEventBus.onStateChanged(calendarEvent);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        Bundle bundle2 = new Bundle();
        bundle2.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, false);
        CalendarEvent calendarEvent2 = new CalendarEvent(bundle2);
        mEventBus.onStateChanged(calendarEvent2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(mMockObserver, times(4)).onStateChanged(captor.capture());
        assertEquals(0, mRingerController.mEventMap.size());
    }

    @Test
    public void testOnStateChanged_CalendarEventDrivingEventCalendarEventFree_ShouldNotMute() {
        when(mMockSettings.getSharedPreferences()).thenReturn(mMockSharedPreferences);
        when(mMockSharedPreferences.getBoolean(
                eq(RingerController.SETTINGS_MUTE_ALL_BUSY_CALENDAR_EVENT_BOOLEAN),
                any(Boolean.class))).thenReturn(true);
        when(mMockSharedPreferences.getBoolean(
                eq(RingerController.SETTINGS_SHOULD_MUTE_WHEN_DRIVING_BOOLEAN),
                any(Boolean.class))).thenReturn(true);
        mEventBus = EventBus.getEventBus();
        mEventBus.reset();
        mEventBus.registerObserver(mMockObserver);
        mRingerController = new RingerController(getContext(), mEventBus, mMockSettings);
        mEventBus.registerUpdater(mRingerController);
        mEventBus.registerObserver(mRingerController);
        Bundle bundle = new Bundle();
        bundle.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, true);
        CalendarEvent calendarEvent = new CalendarEvent(bundle);
        mEventBus.onStateChanged(calendarEvent);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        Bundle bundle2 = new Bundle();
        bundle2.putBoolean(DrivingEvent.KEY_DRIVING_BOOLEAN, true);
        DrivingEvent drivingEvent = new DrivingEvent(bundle2);
        mEventBus.onStateChanged(drivingEvent);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        Bundle bundle3 = new Bundle();
        bundle3.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, false);
        CalendarEvent calendarEvent2 = new CalendarEvent(bundle3);
        mEventBus.onStateChanged(calendarEvent2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        assertEquals(1, mRingerController.mEventMap.size());
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        // verify(mMockObserver, times(4)).onStateChanged(captor.capture());
        List<Event> events = captor.getAllValues();
        for (int i = 0; i < events.size(); i++) {
            assertFalse(events.get(i).getAdditionalInfo().getBoolean(
                    RingerControlEvent.KEY_RINGER_MODE_REVERT_BOOLEAN, false));
        }
    }
}
