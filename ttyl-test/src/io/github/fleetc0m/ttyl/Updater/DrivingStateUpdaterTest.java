package io.github.fleetc0m.ttyl.Updater;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.test.AndroidTestCase;
import io.github.fleetc0m.ttyl.core.EventBus;
import io.github.fleetc0m.ttyl.events.DrivingEvent;
import io.github.fleetc0m.ttyl.events.Event;
import io.github.fleetc0m.ttyl.updater.DrivingStateUpdater;
import io.github.fleetc0m.ttyl.util.DrivingStateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link io.github.fleetc0m.ttyl.updater.DrivingStateUpdater}
 */
public class DrivingStateUpdaterTest extends AndroidTestCase {
    private static final float DRIVING_METER_PER_SEC = 20.1168f;

    @Mock private Context mMockContext;
    @Mock private LocationManager mMockLocationManager;
    @Mock private Location mMockLocation;
    @Mock private EventBus.Observer mMockObserver;

    private EventBus mEventBus;
    private DrivingStateUpdater mDrivingStateUpdater;
    private DrivingStateUtil mDrivingStateUtil;

    @Before
    @Override
    public void setUp() {
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
        MockitoAnnotations.initMocks(this);
        mDrivingStateUtil = new DrivingStateUtil(getContext());
        when(mMockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(
                mMockLocationManager);
        when(mMockObserver.shouldResponseTo(any(String.class))).thenReturn(true);
        mEventBus = EventBus.getEventBus();
        mDrivingStateUpdater = new DrivingStateUpdater(mMockContext, mEventBus);
        mEventBus.registerUpdater(mDrivingStateUpdater);
        mEventBus.registerObserver(mMockObserver);
    }

    @Test
    public void testOnLocationChanged_StartDriving() {
        when(mMockLocation.hasSpeed()).thenReturn(true);
        when(mMockLocation.getSpeed()).thenReturn(DRIVING_METER_PER_SEC);

        mDrivingStateUpdater.mLocationListener.onLocationChanged(mMockLocation);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(mMockObserver).onStateChanged(captor.capture());
        DrivingEvent drivingEvent = (DrivingEvent) captor.getValue();
        assertTrue(drivingEvent.getAdditionalInfo().getBoolean(DrivingEvent.KEY_DRIVING_BOOLEAN));
        assertEquals(mDrivingStateUtil.meterPerSecToMilePerHour(DRIVING_METER_PER_SEC),
                drivingEvent.getAdditionalInfo().getDouble(DrivingEvent.KEY_DRIVING_SPEED_DOUBLE),
                1.0d);
    }

    @Test
    public void testOnLocationChanged_EndDriving() {
        when(mMockLocation.hasSpeed()).thenReturn(true);
        when(mMockLocation.getSpeed()).thenReturn(DRIVING_METER_PER_SEC);

        mDrivingStateUpdater.mLocationListener.onLocationChanged(mMockLocation);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        when(mMockLocation.hasSpeed()).thenReturn(true);
        when(mMockLocation.getSpeed()).thenReturn(0f);
        mDrivingStateUpdater.mLocationListener.onLocationChanged(mMockLocation);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(mMockObserver, times(2)).onStateChanged(captor.capture());
        DrivingEvent drivingEvent = (DrivingEvent) captor.getValue();
        assertFalse(drivingEvent.getAdditionalInfo().getBoolean(DrivingEvent.KEY_DRIVING_BOOLEAN));
    }
}
