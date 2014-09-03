package io.github.fleetc0m.ttyl.core;

import android.test.AndroidTestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link io.github.fleetc0m.ttyl.core.Settings}
 */
public class SettingsTest extends AndroidTestCase {

    @Before
    @Override
    public void setUp() {
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetSettings() {
        Settings settings = Settings.getSettings(getContext());
        assertNotNull(settings);
    }
}
