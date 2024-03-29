package io.github.fleetc0m.ttyl.core;

import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import io.github.fleetc0m.ttyl.events.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Event bus handles all incoming events from Updater and propagate events to all observers.
 */
public class EventBus {
    public static final String TAG = "EventBus";

    /**
     * Updater pushes an event onto the event bus if something interesting happens.
     */
    public static interface Updater {

    }

    /**
     * Observer listens on the event bus once it's registered to the event bus. It handles events
     * it cares.
     */
    public static interface Observer {
        public void onStateChanged(Event event);
        public boolean shouldResponseTo(String eventType);
    }

    private static EventBus mEventBus;
    private final List<Updater> mUpdaterList;
    @VisibleForTesting public final List<Observer> mObserverList;

    protected EventBus() {
        mUpdaterList = new ArrayList<Updater>();
        mObserverList = new ArrayList<Observer>();
    }

    public synchronized void registerUpdater(Updater updater) {
        mUpdaterList.add(updater);
    }

    public synchronized void registerObserver(Observer observer) {
        mObserverList.add(observer);
    }

    public synchronized void onStateChanged(Event event) {
        Log.d(TAG, String.format("onStateChanged: %s", event.getEventType()));
        for (Observer observer : mObserverList) {
            if (observer.shouldResponseTo(event.getEventType())) {
                observer.onStateChanged(event);
            }
        }
    }

    public synchronized void reset() {
        mUpdaterList.clear();
        mObserverList.clear();
    }

    public static synchronized EventBus getEventBus() {
        if (mEventBus != null) {
            return mEventBus;
        }
        mEventBus = new EventBus();
        return mEventBus;
    }
}
