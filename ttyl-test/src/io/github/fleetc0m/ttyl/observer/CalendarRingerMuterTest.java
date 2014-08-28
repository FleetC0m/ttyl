package io.github.fleetc0m.ttyl.observer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.test.AndroidTestCase;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.CalendarEvent;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.events.RingerChangeEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link io.github.fleetc0m.ttyl.observer.CalendarRingerMuter}
 */
public class CalendarRingerMuterTest extends AndroidTestCase {
    private EventBus mEventBus;
    private CalendarRingerMuter mCalendarRingerMuter;

    @Mock private AudioManager mMockAudioManager;
    @Mock private Context mMockContext;
    @Mock private EventBus.Observer mMockObserver;

    @Override
    @Before
    public void setUp() {
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
        MockitoAnnotations.initMocks(this);
        when(mMockContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(mMockAudioManager);
        when(mMockObserver.shouldResponseTo(any(String.class))).thenReturn(true);
        mEventBus = EventBus.getEventBus();
        mCalendarRingerMuter = new CalendarRingerMuter(mMockContext, mEventBus);
        mEventBus.registerObserver(mCalendarRingerMuter);
        mEventBus.registerUpdater(mCalendarRingerMuter);
        mEventBus.registerObserver(mMockObserver);
    }

    @Test
    public void testOnStateChanged_CalendarEventBusyOrigRingerNotSilence() {
        when(mMockAudioManager.getRingerMode()).thenReturn(AudioManager.RINGER_MODE_NORMAL);

        Bundle bundle = new Bundle();
        bundle.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, true);
        CalendarEvent calendarEvent = new CalendarEvent(bundle);
        mEventBus.onStateChanged(calendarEvent);

        ArgumentCaptor<Event> eventArgumentCaptor =
                ArgumentCaptor.forClass(Event.class);
        // This is an ugly way to wait for all the worker threads to finish.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Nothing
        }
        verify(mMockAudioManager).setRingerMode(AudioManager.RINGER_MODE_SILENT);
        verify(mMockObserver, times(2)).onStateChanged(eventArgumentCaptor.capture());
        // There should be two events committed onto the eventBus
        // 0. CalendarEvent 1. RingerChangeEvent
        Event resultCalendarEvent = eventArgumentCaptor.getAllValues().get(0);
        assertEquals(Event.CALENDAR_ENTRY, resultCalendarEvent.getEventType());
        assertTrue(resultCalendarEvent.getAdditionalInfo().getBoolean(
                CalendarEvent.KEY_BUSY_BOOLEAN));

        Event ringerChangeEvent = eventArgumentCaptor.getAllValues().get(1);
        assertEquals(Event.RINGER_CHANGE, ringerChangeEvent.getEventType());
        assertEquals(AudioManager.RINGER_MODE_NORMAL,
                ringerChangeEvent.getAdditionalInfo().getInt(
                        RingerChangeEvent.KEY_ORIG_RINGER_MODE_INT));
        assertEquals(AudioManager.RINGER_MODE_SILENT,
                ringerChangeEvent.getAdditionalInfo().getInt(
                        RingerChangeEvent.KEY_NEW_RINGER_MODE_INT));
    }

    @Test
    public void testOnStateChanged_CalendarEventBusyOrigRingerSilence() {
        when(mMockAudioManager.getRingerMode()).thenReturn(AudioManager.RINGER_MODE_SILENT);

        Bundle bundle = new Bundle();
        bundle.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, true);
        CalendarEvent calendarEvent = new CalendarEvent(bundle);
        mEventBus.onStateChanged(calendarEvent);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Nothing
        }

        // Make sure we don't silence the ringer again if it's already silenced.
        verify(mMockAudioManager, never()).setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    @Test
    public void testOnStateChanged_CalendarEventBusyBusyFree_shouldSilenceRinger() {
        when(mMockAudioManager.getRingerMode()).thenReturn(AudioManager.RINGER_MODE_NORMAL);

        Bundle bundle1 = new Bundle();
        bundle1.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, true);
        CalendarEvent calendarEvent1 = new CalendarEvent(bundle1);
        mEventBus.onStateChanged(calendarEvent1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Nothing
        }
        // The actual ringer should be silenced now.
        when(mMockAudioManager.getRingerMode()).thenReturn(AudioManager.RINGER_MODE_SILENT);

        Bundle bundle2 = new Bundle();
        bundle2.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, true);
        CalendarEvent calendarEvent2 = new CalendarEvent(bundle2);
        mEventBus.onStateChanged(calendarEvent2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Nothing
        }
        // We should not silence the ringer again, but it should be in the silence state.

        Bundle bundle3 = new Bundle();
        bundle3.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, false);
        CalendarEvent calendarEvent3 = new CalendarEvent(bundle3);
        mEventBus.onStateChanged(calendarEvent3);
        // Make sure if we see two busy event and then a free event, we don't revert the ringer.
        verify(mMockAudioManager, never()).setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
}
