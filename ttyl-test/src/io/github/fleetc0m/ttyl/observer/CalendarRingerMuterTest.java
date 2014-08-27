package io.github.fleetc0m.ttyl.observer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.test.AndroidTestCase;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.CalendarEvent;
import io.github.fleetc0m.ttyl.events.RingerChangeEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
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
    public void testOnStateChanged_CalenderEventBusyOrigRingerNotSilence() {
        when(mMockContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(mMockAudioManager);
        when(mMockAudioManager.getRingerMode()).thenReturn(AudioManager.RINGER_MODE_NORMAL);

        Bundle bundle = new Bundle();
        bundle.putBoolean(CalendarEvent.KEY_BUSY_BOOLEAN, true);
        CalendarEvent calendarEvent = new CalendarEvent(bundle);
        mEventBus.onStateChanged(calendarEvent);

        ArgumentCaptor<RingerChangeEvent> ringerChangeEventArgumentCaptor =
                ArgumentCaptor.forClass(RingerChangeEvent.class);
        // There should be a call to mMockAudioManager.setRingerMode(RINGER_MODE_SILENCE)
        // because we have a busy calendar event. But for some reason this function is not being
        // called.
        verify(mMockAudioManager).setRingerMode(AudioManager.RINGER_MODE_SILENT);
        // verify(mMockObserver).onStateChanged(ringerChangeEventArgumentCaptor.capture());
        // There should be two events committed onto the eventBus
        // 0. CalendarEvent 1. RingerChangeEvent
        // RingerChangeEvent ringerChangeEvent = ringerChangeEventArgumentCaptor.getAllValues().get(1);
        // assertEquals(AudioManager.RINGER_MODE_NORMAL,
        //        ringerChangeEvent.getAdditionalInfo().getInt(
        //                RingerChangeEvent.KEY_ORIG_RINGER_MODE_INT));
        //assertEquals(AudioManager.RINGER_MODE_SILENT,
        //        ringerChangeEvent.getAdditionalInfo().getInt(
        //                RingerChangeEvent.KEY_NEW_RINGER_MODE_INT));
    }
}
