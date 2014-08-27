package io.github.fleetc0m.ttyl;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class io.github.fleetc0m.ttyl.HomeActivityTest \
 * io.github.fleetc0m.ttyl.tests/android.test.InstrumentationTestRunner
 */
public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    public HomeActivityTest() {
        super("io.github.fleetc0m.ttyl", HomeActivity.class);
    }

}
