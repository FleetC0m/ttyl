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
        // verify(mMockAudioManager).setRingerMode(AudioManager.RINGER_MODE_SILENT);
        verify(mMockObserver).onStateChanged(ringerChangeEventArgumentCaptor.capture());
        RingerChangeEvent ringerChangeEvent = ringerChangeEventArgumentCaptor.getAllValues().get();
        assertEquals(AudioManager.RINGER_MODE_NORMAL,
                ringerChangeEvent.getAdditionalInfo().getInt(
                        RingerChangeEvent.KEY_ORIG_RINGER_MODE_INT));
        assertEquals(AudioManager.RINGER_MODE_SILENT,
                ringerChangeEvent.getAdditionalInfo().getInt(
                        RingerChangeEvent.KEY_NEW_RINGER_MODE_INT));
    }
}
