package io.github.fleetc0m.ttyl.core;

import android.test.AndroidTestCase;
import io.github.fleetc0m.ttyl.events.Event;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link io.github.fleetc0m.ttyl.core.EventBus}
 */
public class EventBusTest extends AndroidTestCase {
    private EventBus mEventbus;

    @Mock private EventBus.Observer mMockObserver;
    @Mock private EventBus.Updater mMockUpdater;
    @Mock private Event mMockEvent;

    @Override
    public void setUp() {
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
        MockitoAnnotations.initMocks(this);
        mEventbus = EventBus.getEventBus();
        mEventbus.registerObserver(mMockObserver);
        mEventbus.registerUpdater(mMockUpdater);
    }

    @Test
    public void testOnStateChanged() {
        when(mMockObserver.shouldResponseTo(any(String.class))).thenReturn(true);
        when(mMockEvent.getEventType()).thenReturn("test_event");
        mEventbus.onStateChanged(mMockEvent);
        verify(mMockObserver).onStateChanged(mMockEvent);
    }
}
