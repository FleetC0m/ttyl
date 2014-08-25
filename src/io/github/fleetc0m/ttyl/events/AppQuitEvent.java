package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * Event which user decide to stop the app and its services.
 */
public class AppQuitEvent extends Event{
    @Override
    public int getEventType() {
        return Event.APP_QUIT;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return new Bundle();
    }
}
