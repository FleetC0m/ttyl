package io.github.fleetc0m.ttyl.events;

import android.os.Bundle;

/**
 * Event which user decide to stop the app and its services.
 */
public class AppQuitEvent extends Event{
    @Override
    public String getEventType() {
        return Event.QUIT;
    }

    @Override
    public Bundle getAdditionalInfo() {
        return new Bundle();
    }
}
